package com.careerskillroadmap.assessment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.careerskillroadmap.career.Career;
import com.careerskillroadmap.career.Skill;
import com.careerskillroadmap.auth.UserAccountRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class AssessmentServiceTest {

    @Test
    void selectsRecommendedCareerAndReturnsItsOrderedCatalogSkillsWithoutMergingManagedAssessment() {
        Career career = mock(Career.class);
        given(career.getId()).willReturn(1L);
        given(career.getTitle()).willReturn("Embedded Systems Engineer");
        given(career.getDomain()).willReturn("Embedded Systems");
        given(career.getDescription()).willReturn("Build software for embedded devices.");
        given(career.getSkills()).willReturn(List.of(
                new Skill("C programming", "Learn embedded C.", 1),
                new Skill("Firmware debugging", "Debug device firmware.", 2)));

        Assessment assessment = spy(new Assessment(
                List.of("embedded"), "firmware", "", "Beginner", List.of("projects")));
        List<String> domains = new ArrayList<>(List.of("embedded"));
        List<String> goals = new ArrayList<>(List.of("projects"));
        given(assessment.getDomains()).willReturn(domains);
        given(assessment.getGoals()).willReturn(goals);
        assessment.addRecommendation(new AssessmentRecommendation(career, 90, "Domain match.", 1));

        AssessmentRepository repository = mock(AssessmentRepository.class);
        given(repository.findByIdAndOwner_EmailIgnoreCase(7L, "student@example.com"))
                .willReturn(Optional.of(assessment));
        AssessmentService service = new AssessmentService(
                repository, mock(CareerRecommendationService.class), mock(UserAccountRepository.class),
                mock(com.careerskillroadmap.companies.CompanyPreferenceService.class));

        AssessmentResponse result = service.selectRoadmap(7L, 1L, "student@example.com");

        assertThat(assessment.getRoadmapCareer()).isSameAs(career);
        assertThat(result.selectedCareerId()).isEqualTo(1L);
        assertThat(result.roadmap())
                .extracting(skill -> skill.title())
                .containsExactly("C programming", "Firmware debugging");
        assertThat(result.roadmap())
                .extracting(skill -> skill.position())
                .containsExactly(1, 2);
        assertThat(result.domains()).containsExactly("embedded");
        assertThat(result.goals()).containsExactly("projects");
        assertThat(result.domains()).isNotSameAs(domains);
        assertThat(result.goals()).isNotSameAs(goals);
        verify(repository).findByIdAndOwner_EmailIgnoreCase(7L, "student@example.com");
        verifyNoMoreInteractions(repository);
    }
}
