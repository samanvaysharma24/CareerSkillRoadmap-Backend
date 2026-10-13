package com.careerskillroadmap.assessment;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.careerskillroadmap.career.Career;
import com.careerskillroadmap.career.CareerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CareerRecommendationService {

    private static final Map<String, String> DOMAIN_LABELS = Map.ofEntries(
            Map.entry("ece", "ECE"),
            Map.entry("embedded", "Embedded Systems"),
            Map.entry("iot", "IoT"),
            Map.entry("vlsi", "VLSI"),
            Map.entry("robotics", "Robotics"),
            Map.entry("electrical", "Electrical"),
            Map.entry("software", "Software Development"),
            Map.entry("ai", "AI/ML"),
            Map.entry("cybersecurity", "Cybersecurity"),
            Map.entry("cloud", "Cloud/Networking"),
            Map.entry("data", "Data Science"),
            Map.entry("product", "UI/UX"),
            Map.entry("custom", "Custom"));

    private static final Map<String, Set<String>> GOAL_TERMS = Map.of(
            "projects", Set.of("build", "create", "develop", "prototype", "project"),
            "hands-on", Set.of("circuit", "device", "devices", "sensor", "robot", "test", "board", "equipment"),
            "research", Set.of("research", "signals", "model", "analysis", "machine", "learning"),
            "design", Set.of("design", "user", "interface", "circuit", "digital", "product"),
            "analysis", Set.of("analyze", "analysis", "statistics", "data", "test", "patterns"),
            "career-ready", Set.of("software", "engineer", "developer", "design", "deploy", "testing"));

    private static final Map<String, String> GOAL_LABELS = Map.of(
            "projects", "Build practical projects",
            "hands-on", "Hands-on hardware and lab work",
            "research", "Explore research and new technology",
            "design", "Design user-friendly products",
            "analysis", "Analyze data and solve problems",
            "career-ready", "Prepare for internships and jobs");

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "for", "in", "is", "of", "or", "the", "to", "with", "my", "i");

    private final CareerRepository careerRepository;

    public CareerRecommendationService(CareerRepository careerRepository) {
        this.careerRepository = careerRepository;
    }

    public List<Match> recommend(AssessmentRequest request) {
        return recommend(request, Set.of());
    }

    public List<Match> recommend(AssessmentRequest request, Set<String> preferredCompanyDomains) {
        Set<String> selectedDomains = request.domains().stream()
                .map(domain -> DOMAIN_LABELS.get(domain))
                .collect(Collectors.toSet());
        Set<String> interestTerms = tokens(request.interests() + " " + request.customInterest());
        Set<String> selectedGoals = Set.copyOf(request.goals());

        return careerRepository.findAll().stream()
                .map(career -> score(career, selectedDomains, interestTerms, selectedGoals,
                        request.skillLevel(), preferredCompanyDomains))
                .filter(match -> match.domainMatch() || !match.interestMatches().isEmpty())
                .sorted((left, right) -> {
                    int byScore = Integer.compare(right.score(), left.score());
                    return byScore != 0 ? byScore : left.career().getTitle().compareToIgnoreCase(right.career().getTitle());
                })
                .toList();
    }

    private Match score(Career career, Set<String> selectedDomains, Set<String> interestTerms,
                        Set<String> selectedGoals, String skillLevel, Set<String> preferredCompanyDomains) {
        boolean domainMatch = selectedDomains.contains(career.getDomain());
        boolean companyMatch = preferredCompanyDomains.stream()
                .map(DOMAIN_LABELS::get)
                .anyMatch(career.getDomain()::equals);
        Set<String> careerTerms = tokens(career.getTitle() + " " + career.getDomain() + " " + career.getDescription()
                + " " + career.getSkills().stream()
                        .map(skill -> skill.getTitle() + " " + skill.getDescription())
                        .collect(Collectors.joining(" ")));
        List<String> matchedInterests = interestTerms.stream()
                .filter(careerTerms::contains)
                .sorted()
                .toList();
        List<String> matchedGoals = selectedGoals.stream()
                .filter(goal -> GOAL_TERMS.get(goal).stream().anyMatch(careerTerms::contains))
                .sorted()
                .toList();

        int score = 50 + (domainMatch ? 30 : 0) + (companyMatch ? 10 : 0) + Math.min(matchedInterests.size(), 4) * 5
                + Math.min(matchedGoals.size(), 3) * 5;
        String reason = explain(career, domainMatch, companyMatch, matchedInterests, matchedGoals, skillLevel);
        return new Match(career, Math.min(score, 100), reason, domainMatch, matchedInterests);
    }

    private String explain(Career career, boolean domainMatch, boolean companyMatch, List<String> matchedInterests,
                           List<String> matchedGoals, String skillLevel) {
        List<String> reasons = new java.util.ArrayList<>();
        if (domainMatch) {
            reasons.add("your selected " + career.getDomain() + " domain");
        }
        if (companyMatch) {
            reasons.add("a domain connected to your target companies");
        }
        if (!matchedInterests.isEmpty()) {
            reasons.add("interest terms: " + String.join(", ", matchedInterests));
        }
        if (!matchedGoals.isEmpty()) {
            reasons.add("learning goal" + (matchedGoals.size() > 1 ? "s" : "") + ": "
                    + matchedGoals.stream().map(GOAL_LABELS::get).collect(Collectors.joining(", ")));
        }
        if (reasons.isEmpty()) {
            return "No matching domain, interest, or goal was found.";
        }
        return "Matches " + String.join("; ", reasons) + ". Your " + skillLevel.toLowerCase(Locale.ROOT)
                + " skill level is saved for roadmap context.";
    }

    private Set<String> tokens(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        return Arrays.stream(normalized.split("[^a-z0-9+#.]+"))
                .map(token -> token.replaceAll("^\\.+|\\.+$", ""))
                .filter(token -> token.length() > 1 || token.equals("c"))
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toSet());
    }

    public record Match(Career career, int score, String reason, boolean domainMatch, List<String> interestMatches) {
    }
}
