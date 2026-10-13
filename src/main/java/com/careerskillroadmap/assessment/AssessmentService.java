package com.careerskillroadmap.assessment;

import java.util.ArrayList;
import java.util.List;

import com.careerskillroadmap.auth.UserAccount;
import com.careerskillroadmap.auth.UserAccountRepository;
import com.careerskillroadmap.companies.CompanyPreferenceService;
import com.careerskillroadmap.career.Career;
import com.careerskillroadmap.career.CareerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final CareerRecommendationService recommendationService;
    private final UserAccountRepository userAccountRepository;
    private final CompanyPreferenceService companyPreferenceService;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             CareerRecommendationService recommendationService,
                             UserAccountRepository userAccountRepository,
                             CompanyPreferenceService companyPreferenceService) {
        this.assessmentRepository = assessmentRepository;
        this.recommendationService = recommendationService;
        this.userAccountRepository = userAccountRepository;
        this.companyPreferenceService = companyPreferenceService;
    }

    public AssessmentResponse create(AssessmentRequest request, String ownerEmail) {
        UserAccount owner = userAccountRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated account was not found."));
        Assessment assessment = new Assessment(
                request.domains().stream().distinct().toList(),
                request.interests(),
                request.customInterest(),
                request.skillLevel(),
                request.goals().stream().distinct().toList());
        assessment.setOwner(owner);
        List<CareerRecommendationService.Match> matches = recommendationService.recommend(
                request, companyPreferenceService.preferredDomains(ownerEmail));
        for (int index = 0; index < matches.size(); index++) {
            CareerRecommendationService.Match match = matches.get(index);
            assessment.addRecommendation(new AssessmentRecommendation(
                    match.career(), match.score(), match.reason(), index + 1));
        }
        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public AssessmentResponse findById(Long id, String ownerEmail) {
        return toResponse(findAssessment(id, ownerEmail));
    }

    public AssessmentResponse selectRoadmap(Long assessmentId, Long careerId, String ownerEmail) {
        Assessment assessment = findAssessment(assessmentId, ownerEmail);
        Career selectedCareer = assessment.getRecommendations().stream()
                .map(AssessmentRecommendation::getCareer)
                .filter(career -> career.getId().equals(careerId))
                .findFirst()
                .orElseThrow(() -> new InvalidRoadmapSelectionException(careerId));
        assessment.setRoadmapCareer(selectedCareer);
        return toResponse(assessment);
    }

    public AssessmentResponse refreshRecommendations(String ownerEmail) {
        UserAccount owner = userAccountRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated account was not found."));
        Assessment assessment = assessmentRepository.findFirstByOwner_IdOrderByIdDesc(owner.getId())
                .orElseThrow(() -> new AssessmentNotFoundException(0L));
        AssessmentRequest request = new AssessmentRequest(
                assessment.getDomains(),
                assessment.getInterests(),
                assessment.getCustomInterest(),
                assessment.getSkillLevel(),
                assessment.getGoals());
        List<CareerRecommendationService.Match> matches = recommendationService.recommend(
                request, companyPreferenceService.preferredDomains(ownerEmail));

        for (AssessmentRecommendation recommendation : new ArrayList<>(assessment.getRecommendations())) {
            recommendation.setAssessment(null);
        }
        assessment.clearRecommendations();
        assessmentRepository.flush();

        for (int index = 0; index < matches.size(); index++) {
            CareerRecommendationService.Match match = matches.get(index);
            assessment.addRecommendation(new AssessmentRecommendation(
                    match.career(), match.score(), match.reason(), index + 1));
        }
        if (assessment.getRoadmapCareer() != null
                && matches.stream().noneMatch(match -> match.career().getId().equals(assessment.getRoadmapCareer().getId()))) {
            assessment.setRoadmapCareer(null);
        }
        return toResponse(assessment);
    }

    private Assessment findAssessment(Long id, String ownerEmail) {
        return assessmentRepository.findByIdAndOwner_EmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new AssessmentNotFoundException(id));
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        List<AssessmentResponse.RecommendationResponse> recommendations = assessment.getRecommendations().stream()
                .map(recommendation -> {
                    Career career = recommendation.getCareer();
                    CareerResponse careerResponse = CareerResponse.from(career);
                    return new AssessmentResponse.RecommendationResponse(
                            career.getId(),
                            career.getTitle(),
                            career.getDomain(),
                            career.getDescription(),
                            recommendation.getMatchScore(),
                            recommendation.getReason(),
                            careerResponse.skills());
                })
                .toList();
        Career roadmapCareer = assessment.getRoadmapCareer();
        List<CareerResponse.SkillResponse> roadmap = roadmapCareer == null
                ? List.of()
                : CareerResponse.from(roadmapCareer).skills();
        return new AssessmentResponse(
                assessment.getId(),
                List.copyOf(assessment.getDomains()),
                assessment.getInterests(),
                assessment.getCustomInterest(),
                assessment.getSkillLevel(),
                List.copyOf(assessment.getGoals()),
                recommendations,
                roadmapCareer == null ? null : roadmapCareer.getId(),
                roadmap);
    }
}
