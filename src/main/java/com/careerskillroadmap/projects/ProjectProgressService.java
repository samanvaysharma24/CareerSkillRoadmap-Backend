package com.careerskillroadmap.projects;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.careerskillroadmap.assessment.Assessment;
import com.careerskillroadmap.assessment.AssessmentRepository;
import com.careerskillroadmap.auth.UserAccount;
import com.careerskillroadmap.auth.UserAccountRepository;
import com.careerskillroadmap.companies.CompanyPreferenceRepository;
import com.careerskillroadmap.companies.CompanyCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectProgressService {

    private static final int MILESTONE_XP = 25;
    private static final int PROJECT_COMPLETION_XP = 100;
    private static final int MAX_LEVEL = 30;

    private final ProjectCatalog catalog;
    private final ProjectEnrollmentRepository enrollmentRepository;
    private final XpRewardRepository rewardRepository;
    private final UserAccountRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final CompanyPreferenceRepository companyPreferenceRepository;
    private final CompanyCatalog companyCatalog;

    public ProjectProgressService(
            ProjectCatalog catalog,
            ProjectEnrollmentRepository enrollmentRepository,
            XpRewardRepository rewardRepository,
            UserAccountRepository userRepository,
            AssessmentRepository assessmentRepository,
            CompanyPreferenceRepository companyPreferenceRepository,
            CompanyCatalog companyCatalog) {
        this.catalog = catalog;
        this.enrollmentRepository = enrollmentRepository;
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.companyPreferenceRepository = companyPreferenceRepository;
        this.companyCatalog = companyCatalog;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(String email) {
        UserAccount user = user(email);
        Assessment latest = assessmentRepository.findFirstByOwner_IdOrderByIdDesc(user.getId()).orElse(null);
        Set<String> selectedDomains = latest == null ? Set.of() : Set.copyOf(latest.getDomains());
        Set<String> goals = latest == null ? Set.of() : Set.copyOf(latest.getGoals());
        Set<String> companyDomains = companyPreferenceRepository.findAllByOwner_Id(user.getId()).stream()
                .map(preference -> companyCatalog.find(preference.getCompanyKey()))
                .flatMap(company -> company.domains().stream())
                .collect(Collectors.toSet());
        Map<String, ProjectEnrollment> enrollments = enrollmentRepository.findAllByOwner_Id(user.getId()).stream()
                .collect(Collectors.toMap(ProjectEnrollment::getProjectKey, Function.identity()));

        return catalog.all().stream()
                .map(project -> toResponse(project, enrollments.get(project.key()), selectedDomains, goals, companyDomains))
                .sorted((left, right) -> {
                    int leftScore = recommendationScore(left, selectedDomains, goals, companyDomains);
                    int rightScore = recommendationScore(right, selectedDomains, goals, companyDomains);
                    return Integer.compare(rightScore, leftScore);
                })
                .toList();
    }

    public ProjectResponse start(String email, String projectKey) {
        ProjectDefinition definition = catalog.find(projectKey);
        UserAccount user = user(email);
        ProjectEnrollment enrollment = enrollmentRepository.findForUpdate(user.getId(), projectKey)
                .orElseGet(() -> enrollmentRepository.save(new ProjectEnrollment(user, projectKey)));
        return responseFor(user, definition, enrollment);
    }

    public ProjectResponse completeMilestone(String email, String projectKey, String milestoneKey) {
        ProjectDefinition definition = catalog.find(projectKey);
        ProjectDefinition.MilestoneDefinition milestone = definition.milestones().stream()
                .filter(item -> item.key().equals(milestoneKey))
                .findFirst()
                .orElseThrow(() -> new InvalidProjectMilestoneException());
        UserAccount user = user(email);
        ProjectEnrollment enrollment = enrollmentRepository.findForUpdate(user.getId(), projectKey)
                .orElseThrow(() -> new ProjectNotStartedException());

        if (!enrollment.getCompletedMilestones().contains(milestone.key())) {
            boolean allMilestonesComplete = definition.milestones().stream()
                    .allMatch(item -> item.key().equals(milestone.key())
                            || enrollment.getCompletedMilestones().contains(item.key()));
            enrollment.completeMilestone(milestone.key(), allMilestonesComplete);
            award(user, "project:" + projectKey + ":milestone:" + milestone.key(),
                    definition.title() + ": " + milestone.title(), MILESTONE_XP);
            if (allMilestonesComplete) {
                award(user, "project:" + projectKey + ":complete",
                        definition.title() + " completed", PROJECT_COMPLETION_XP);
            }
        }
        return responseFor(user, definition, enrollment);
    }

    @Transactional(readOnly = true)
    public XpSummary summary(String email) {
        UserAccount user = user(email);
        List<XpReward> rewards = rewardRepository.findAllByOwner_IdOrderByAwardedAtDesc(user.getId());
        long totalXp = rewards.stream().mapToLong(XpReward::getPoints).sum();
        int level = levelFor(totalXp);
        long floor = xpAtLevel(level);
        long next = level == MAX_LEVEL ? floor : xpAtLevel(level + 1);
        int progress = level == MAX_LEVEL ? 100
                : (int) Math.min(100, (totalXp - floor) * 100 / (next - floor));
        List<Badge> badges = List.of(
                new Badge("first-milestone", "First Step", "Complete your first project milestone.", rewards.size() >= 1),
                new Badge("first-project", "Project Finisher", "Complete your first project.", rewards.stream()
                        .anyMatch(reward -> reward.getEventKey().endsWith(":complete"))),
                new Badge("xp-500", "Rising Builder", "Earn 500 XP from projects.", totalXp >= 500),
                new Badge("xp-2000", "Dedicated Maker", "Earn 2,000 XP from projects.", totalXp >= 2000));
        return new XpSummary(totalXp, level, rank(level), progress, xpForNextLevel(totalXp, level),
                rewards.stream().map(reward -> new RewardResponse(
                        reward.getEventKey(), reward.getDescription(), reward.getPoints(), reward.getAwardedAt())).toList(),
                badges);
    }

    static int levelFor(long totalXp) {
        int level = 1;
        while (level < MAX_LEVEL && totalXp >= xpAtLevel(level + 1)) level++;
        return level;
    }

    static long xpAtLevel(int level) {
        return 100L * (level - 1) * level / 2;
    }

    private long xpForNextLevel(long xp, int level) {
        return level == MAX_LEVEL ? 0 : xpAtLevel(level + 1) - xp;
    }

    private String rank(int level) {
        if (level <= 10) return "Beginner";
        if (level <= 20) return "Intermediate";
        return "Advanced";
    }

    private UserAccount user(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated account not found."));
    }

    private void award(UserAccount user, String eventKey, String description, int points) {
        if (!rewardRepository.existsByOwner_IdAndEventKey(user.getId(), eventKey)) {
            rewardRepository.save(new XpReward(user, eventKey, description, points));
        }
    }

    private ProjectResponse toResponse(
            ProjectDefinition definition,
            ProjectEnrollment enrollment,
            Set<String> domains,
            Set<String> goals,
            Set<String> companyDomains) {
        Set<String> completed = enrollment == null ? Set.of() : enrollment.getCompletedMilestones();
        List<MilestoneResponse> milestones = definition.milestones().stream()
                .map(milestone -> new MilestoneResponse(
                        milestone.key(), milestone.title(), milestone.learningStep(), completed.contains(milestone.key())))
                .toList();
        int percent = milestones.isEmpty() ? 0
                : completed.size() * 100 / milestones.size();
        return new ProjectResponse(definition.key(), definition.title(), definition.description(), definition.domain(),
                definition.difficulty(), definition.skillsAndTools(), milestones, definition.expectedOutcome(),
                enrollment != null, enrollment != null && enrollment.getCompletedAt() != null,
                percent, recommendationReason(definition, domains, goals, companyDomains));
    }

    private int recommendationScore(ProjectResponse response, Set<String> domains, Set<String> goals, Set<String> companyDomains) {
        Set<String> projectGoals = catalog.find(response.key()).goals();
        int score = domains.contains(response.domain()) ? 100 : 0;
        if (companyDomains.contains(response.domain())) score += 20;
        if (projectGoals.stream().anyMatch(goals::contains)) score += 5;
        return score;
    }

    private String recommendationReason(ProjectDefinition project, Set<String> domains, Set<String> goals,
                                        Set<String> companyDomains) {
        if (domains.contains(project.domain()) && companyDomains.contains(project.domain())) {
            return "Matches your assessment domain and a selected company domain.";
        }
        if (domains.contains(project.domain())) return "Matches a domain from your latest assessment.";
        if (companyDomains.contains(project.domain())) return "Matches a domain of a company you selected.";
        if (catalog.find(project.key()).goals().stream().anyMatch(goals::contains)) {
            return "Supports one or more learning goals from your latest assessment.";
        }
        return "Explore this curated " + project.difficulty().toLowerCase() + " project.";
    }

    private ProjectResponse responseFor(UserAccount user, ProjectDefinition definition, ProjectEnrollment enrollment) {
        Assessment latest = assessmentRepository.findFirstByOwner_IdOrderByIdDesc(user.getId()).orElse(null);
        Set<String> domains = latest == null ? Set.of() : Set.copyOf(latest.getDomains());
        Set<String> goals = latest == null ? Set.of() : Set.copyOf(latest.getGoals());
        Set<String> companyDomains = companyPreferenceRepository.findAllByOwner_Id(user.getId()).stream()
                .map(preference -> companyCatalog.find(preference.getCompanyKey()))
                .flatMap(company -> company.domains().stream())
                .collect(Collectors.toSet());
        return toResponse(definition, enrollment, domains, goals, companyDomains);
    }

    public record ProjectResponse(
            String key,
            String title,
            String description,
            String domain,
            String difficulty,
            List<String> skillsAndTools,
            List<MilestoneResponse> milestones,
            String expectedOutcome,
            boolean started,
            boolean completed,
            int progressPercent,
            String recommendationReason) {
    }

    public record MilestoneResponse(String key, String title, String learningStep, boolean completed) {
    }

    public record RewardResponse(String eventKey, String description, int points, java.time.Instant awardedAt) {
    }

    public record Badge(String key, String title, String description, boolean earned) {
    }

    public record XpSummary(
            long totalXp,
            int level,
            String rank,
            int progressPercent,
            long xpToNextLevel,
            List<RewardResponse> rewards,
            List<Badge> badges) {
    }
}
