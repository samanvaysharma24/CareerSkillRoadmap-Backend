package com.careerskillroadmap.api;

import java.util.List;

import com.careerskillroadmap.assessment.AssessmentController;
import com.careerskillroadmap.assessment.AssessmentResponse;
import com.careerskillroadmap.assessment.AssessmentService;
import com.careerskillroadmap.career.CareerResponse;
import com.careerskillroadmap.config.ApiCorsConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssessmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiCorsConfiguration.class, com.careerskillroadmap.assessment.AssessmentExceptionHandler.class})
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentService assessmentService;

    @Test
    void createsAssessmentAndReturnsSavedResponseWithOrderedCareerSkills() throws Exception {
        given(assessmentService.create(any(), anyString())).willReturn(savedAssessment());

        mockMvc.perform(post("/api/assessments")
                        .contentType("application/json")
                        .principal(testPrincipal())
                        .content("""
                                {
                                  "domains":["embedded"],
                                  "interests":"firmware",
                                  "customInterest":"",
                                  "skillLevel":"Beginner",
                                  "goals":["hands-on"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.recommendations[0].careerId").value(12))
                .andExpect(jsonPath("$.recommendations[0].matchScore").value(95))
                .andExpect(jsonPath("$.recommendations[0].reason").value("Matches your selected Embedded Systems domain."))
                .andExpect(jsonPath("$.recommendations[0].skills[0].position").value(1))
                .andExpect(jsonPath("$.selectedCareerId").doesNotExist())
                .andExpect(jsonPath("$.roadmap").isEmpty());
    }

    @Test
    void rejectsAnAssessmentWithoutDomains() throws Exception {
        mockMvc.perform(post("/api/assessments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "domains":[],
                                  "interests":"interests",
                                  "customInterest":"",
                                  "skillLevel":"Beginner",
                                  "goals":["projects"]
                                }
                                """))
                .andExpect(status().isBadRequest());

        then(assessmentService).shouldHaveNoInteractions();
    }

    @Test
    void rejectsUnsupportedAssessmentChoices() throws Exception {
        mockMvc.perform(post("/api/assessments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "domains":["not-a-domain"],
                                  "interests":"circuits",
                                  "customInterest":"",
                                  "skillLevel":"expert",
                                  "goals":["projects"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getsAnAssessmentAndPersistsItsSelectedRoadmap() throws Exception {
        given(assessmentService.findById(7L, "student@example.com")).willReturn(savedAssessment());
        given(assessmentService.selectRoadmap(7L, 12L, "student@example.com")).willReturn(new AssessmentResponse(
                7L, List.of("embedded"), "firmware", "", "Beginner", List.of("hands-on"),
                savedAssessment().recommendations(), 12L,
                List.of(new CareerResponse.SkillResponse(31L, "C programming", "Learn C.", 1))));

        mockMvc.perform(get("/api/assessments/7").principal(testPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations[0].title").value("Embedded Systems Engineer"));

        mockMvc.perform(put("/api/assessments/7/roadmap").principal(testPrincipal())
                        .contentType("application/json")
                        .content("""
                                {"careerId":12}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedCareerId").value(12))
                .andExpect(jsonPath("$.roadmap[0].title").value("C programming"))
                .andExpect(jsonPath("$.roadmap[0].position").value(1));
    }

    @Test
    void roadmapDatabaseFailureIsLoggedAndReturnsSafeProblemDetail() throws Exception {
        willThrow(new DataAccessResourceFailureException("database detail must not be exposed"))
                .given(assessmentService).selectRoadmap(7L, 12L, "student@example.com");

        mockMvc.perform(put("/api/assessments/7/roadmap").principal(testPrincipal())
                        .contentType("application/json")
                        .content("""
                                {"careerId":12}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Assessment persistence failed"))
                .andExpect(jsonPath("$.detail").value(
                        "The assessment could not be saved. Check the server log for details."))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("database detail"))));
    }

    @Test
    void unexpectedAssessmentFailureIsLoggedAndReturnsSafeProblemDetail() throws Exception {
        willThrow(new IllegalStateException("internal detail must not be exposed"))
                .given(assessmentService).selectRoadmap(7L, 12L, "student@example.com");

        mockMvc.perform(put("/api/assessments/7/roadmap").principal(testPrincipal())
                        .contentType("application/json")
                        .content("""
                                {"careerId":12}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Assessment request failed"))
                .andExpect(jsonPath("$.detail").value(
                        "The assessment request could not be completed. Check the server log for details."))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("internal detail"))));
    }

    @Test
    void assessmentEndpointsAllowTheViteDevelopmentOrigin() throws Exception {
        mockMvc.perform(options("/api/assessments")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("content-type")));
    }

    private AssessmentResponse savedAssessment() {
        List<CareerResponse.SkillResponse> skills = List.of(
                new CareerResponse.SkillResponse(31L, "C programming", "Learn C.", 1));
        return new AssessmentResponse(
                7L,
                List.of("embedded"),
                "firmware",
                "",
                "Beginner",
                List.of("hands-on"),
                List.of(new AssessmentResponse.RecommendationResponse(
                        12L,
                        "Embedded Systems Engineer",
                        "Embedded Systems",
                        "Build firmware.",
                        95,
                        "Matches your selected Embedded Systems domain.",
                        skills)),
                null,
                List.of());
    }

    private Authentication testPrincipal() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "student@example.com", "not-used", List.of());
    }
}
