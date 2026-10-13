package com.careerskillroadmap.assessment;

import java.util.List;

import com.careerskillroadmap.career.Career;
import com.careerskillroadmap.career.CareerRepository;
import com.careerskillroadmap.career.Skill;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CareerRecommendationServiceTest {

    @Test
    void ranksDomainInterestAndGoalMatchesAgainstCatalogAndExplainsTheMatch() {
        Career embedded = career("Embedded Systems Engineer", "Embedded Systems",
                "Build firmware for microcontrollers and electronic devices.",
                new Skill("C programming", "Use C for firmware.", 1));
        Career data = career("Data Scientist", "Data Science", "Analyze data and communicate insights.",
                new Skill("Python and data analysis", "Clean and summarize datasets.", 1));
        CareerRepository repository = mock(CareerRepository.class);
        given(repository.findAll()).willReturn(List.of(data, embedded));
        CareerRecommendationService service = new CareerRecommendationService(repository);

        List<CareerRecommendationService.Match> matches = service.recommend(new AssessmentRequest(
                List.of("embedded"), "firmware, microcontrollers", "", "Beginner", List.of("hands-on")));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).career().getTitle()).isEqualTo("Embedded Systems Engineer");
        assertThat(matches.get(0).score()).isEqualTo(95);
        assertThat(matches.get(0).reason())
                .contains("Embedded Systems", "firmware", "microcontrollers", "Hands-on hardware and lab work", "beginner");
    }

    @Test
    void returnsNoRecommendationsWhenNoCatalogDomainOrInterestMatches() {
        Career embedded = career("Embedded Systems Engineer", "Embedded Systems",
                "Build firmware for microcontrollers.", new Skill("C programming", "Use C.", 1));
        CareerRepository repository = mock(CareerRepository.class);
        given(repository.findAll()).willReturn(List.of(embedded));
        CareerRecommendationService service = new CareerRecommendationService(repository);

        assertThat(service.recommend(new AssessmentRequest(
                List.of("custom"), "ocean archaeology", "", "Intermediate", List.of("research"))))
                .isEmpty();
    }

    @Test
    void customInterestsCanMatchCatalogCareersWithoutASelectedDomain() {
        Career data = career("Data Scientist", "Data Science", "Use statistics to uncover patterns.",
                new Skill("Python and data analysis", "Analyze datasets.", 1));
        CareerRepository repository = mock(CareerRepository.class);
        given(repository.findAll()).willReturn(List.of(data));
        CareerRecommendationService service = new CareerRecommendationService(repository);

        List<CareerRecommendationService.Match> matches = service.recommend(new AssessmentRequest(
                List.of("custom"), "", "statistics", "Advanced", List.of("analysis")));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).reason()).contains("statistics", "Analyze data and solve problems", "advanced");
    }

    private Career career(String title, String domain, String description, Skill... skills) {
        Career career = new Career(title, domain, description, title.toLowerCase().replace(' ', '-'));
        for (Skill skill : skills) {
            career.addSkill(skill);
        }
        return career;
    }
}
