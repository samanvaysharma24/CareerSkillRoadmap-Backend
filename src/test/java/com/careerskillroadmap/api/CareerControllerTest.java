package com.careerskillroadmap.api;

import java.util.List;

import com.careerskillroadmap.config.ApiCorsConfiguration;
import com.careerskillroadmap.career.CareerController;
import com.careerskillroadmap.career.CareerNotFoundException;
import com.careerskillroadmap.career.CareerResponse;
import com.careerskillroadmap.career.CareerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CareerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiCorsConfiguration.class)
class CareerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareerService careerService;

    @Test
    void listCareersReturnsDatabaseBackedCareerResponseShape() throws Exception {
        CareerResponse career = new CareerResponse(
                1L,
                "Embedded Systems Engineer",
                "Embedded Systems",
                "Build software for electronic devices.",
                List.of(new CareerResponse.SkillResponse(10L, "C programming", "Learn embedded C.", 1)));
        given(careerService.findAll()).willReturn(List.of(career));

        mockMvc.perform(get("/api/careers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Embedded Systems Engineer"))
                .andExpect(jsonPath("$[0].domain").value("Embedded Systems"))
                .andExpect(jsonPath("$[0].skills[0].title").value("C programming"))
                .andExpect(jsonPath("$[0].skills[0].position").value(1));
    }

    @Test
    void careerApiAllowsTheViteDevelopmentOrigin() throws Exception {
        given(careerService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/careers").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void getCareerReturnsCareerAndItsSkills() throws Exception {
        given(careerService.findById(12L)).willReturn(new CareerResponse(
                12L,
                "UI/UX Designer",
                "UI/UX",
                "Design intuitive digital products.",
                List.of(new CareerResponse.SkillResponse(40L, "User research", "Understand user needs.", 1))));

        mockMvc.perform(get("/api/careers/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.title").value("UI/UX Designer"))
                .andExpect(jsonPath("$.skills[0].title").value("User research"));
    }

    @Test
    void getMissingCareerReturnsNotFoundProblem() throws Exception {
        given(careerService.findById(999L)).willThrow(new CareerNotFoundException(999L));

        mockMvc.perform(get("/api/careers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Career not found"))
                .andExpect(jsonPath("$.detail").value("Career with ID 999 was not found."));
    }

    @Test
    void getCareerWithNonNumericIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/careers/not-a-number"))
                .andExpect(status().isBadRequest());
    }
}
