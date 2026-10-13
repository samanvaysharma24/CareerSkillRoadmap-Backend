package com.careerskillroadmap.auth;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:career-roadmap-auth;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationStoresOnlyBCryptPasswordHash() throws Exception {
        String email = newEmail();
        String password = "safe-password-123";

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));

        UserAccount account = userAccountRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo(password);
        assertThat(account.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches(password, account.getPasswordHash())).isTrue();
    }

    @Test
    void loginCreatesAuthenticatedSessionAndLogoutInvalidatesIt() throws Exception {
        String email = newEmail();
        String password = "safe-password-456";
        register(email, password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateRegistrationAndInvalidLoginReturnSafeErrors() throws Exception {
        String email = newEmail();
        register(email, "safe-password-789");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"another-safe-password"}
                                """.formatted(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An account with this email already exists."));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong-password-123"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email or password is incorrect."));
    }

    @Test
    void corsAllowsCredentialedReactRequestsAndCsrfHeader() throws Exception {
        mockMvc.perform(options("/api/auth/csrf")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Headers",
                                org.hamcrest.Matchers.allOf(
                                        org.hamcrest.Matchers.containsString("content-type"),
                                        org.hamcrest.Matchers.containsString("x-xsrf-token"))));

        mockMvc.perform(get("/api/auth/csrf").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void assessmentCannotBeReadByAnotherAccount() throws Exception {
        String ownerEmail = newEmail();
        String ownerPassword = "owner-password-123";
        register(ownerEmail, ownerPassword);
        MockHttpSession ownerSession = login(ownerEmail, ownerPassword);

        MvcResult assessmentResult = mockMvc.perform(post("/api/assessments")
                        .session(ownerSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "domains":["software"],
                                  "interests":"web development",
                                  "customInterest":"",
                                  "skillLevel":"Beginner",
                                  "goals":["projects"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String response = assessmentResult.getResponse().getContentAsString();
        Number assessmentIdValue = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        long assessmentId = assessmentIdValue.longValue();
        Number recommendedCareerId = com.jayway.jsonpath.JsonPath.read(response, "$.recommendations[0].careerId");

        String otherEmail = newEmail();
        String otherPassword = "other-password-123";
        register(otherEmail, otherPassword);
        MockHttpSession otherSession = login(otherEmail, otherPassword);

        mockMvc.perform(get("/api/assessments/{id}", assessmentId).session(otherSession))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/assessments/{id}/roadmap", assessmentId)
                        .session(otherSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"careerId":%d}
                                """.formatted(recommendedCareerId.longValue())))
                .andExpect(status().isNotFound());
    }

    @Test
    void projectMilestoneXpIsPersistentAndDuplicateCompletionsAreNotRewarded() throws Exception {
        String email = newEmail();
        String password = "project-owner-password";
        register(email, password);
        MockHttpSession session = login(email, password);

        String projectKey = "software-task-api";
        mockMvc.perform(post("/api/projects/{key}/milestones/step-1/complete", projectKey)
                        .session(session).with(csrf()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/projects/{key}/start", projectKey)
                        .session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.started").value(true));

        mockMvc.perform(post("/api/projects/{key}/milestones/step-1/complete", projectKey)
                        .session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestones[0].completed").value(true));
        mockMvc.perform(post("/api/projects/{key}/milestones/step-1/complete", projectKey)
                        .session(session).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/xp").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(25))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.rank").value("Beginner"))
                .andExpect(jsonPath("$.rewards.length()").value(1));

        String otherEmail = newEmail();
        String otherPassword = "different-project-password";
        register(otherEmail, otherPassword);
        MockHttpSession otherSession = login(otherEmail, otherPassword);
        mockMvc.perform(get("/api/projects").session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.key=='software-task-api')].started").value(false));
        mockMvc.perform(get("/api/xp").session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(0));
    }

    @Test
    void completingAllMilestonesAwardsProjectBonusOnlyOnce() throws Exception {
        String email = newEmail();
        String password = "project-complete-password";
        register(email, password);
        MockHttpSession session = login(email, password);
        String projectKey = "iot-room-monitor";
        mockMvc.perform(post("/api/projects/{key}/start", projectKey).session(session).with(csrf()))
                .andExpect(status().isOk());

        for (int step = 1; step <= 3; step++) {
            mockMvc.perform(post("/api/projects/{key}/milestones/step-{step}/complete", projectKey, step)
                            .session(session).with(csrf()))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/xp").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(175))
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.rewards.length()").value(4));
        mockMvc.perform(post("/api/projects/{key}/milestones/step-3/complete", projectKey)
                        .session(session).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/xp").session(session))
                .andExpect(jsonPath("$.totalXp").value(175))
                .andExpect(jsonPath("$.rewards.length()").value(4));
    }

    @Test
    void companyPreferencesArePersistedPerAccountAndCanBeRemoved() throws Exception {
        String email = newEmail();
        String password = "company-owner-password";
        register(email, password);
        MockHttpSession session = login(email, password);

        mockMvc.perform(put("/api/preferences/companies")
                        .session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyKeys":["microsoft","nvidia"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedCompanyKeys.length()").value(2))
                .andExpect(jsonPath("$.notice").value(
                        "Companies are examples only; listing is not an endorsement or a guarantee of employment."));

        mockMvc.perform(get("/api/preferences/companies").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedCompanyKeys").isArray());

        String otherEmail = newEmail();
        String otherPassword = "separate-company-password";
        register(otherEmail, otherPassword);
        MockHttpSession otherSession = login(otherEmail, otherPassword);
        mockMvc.perform(get("/api/preferences/companies").session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedCompanyKeys").isEmpty());

        mockMvc.perform(put("/api/preferences/companies")
                        .session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyKeys":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedCompanyKeys").isEmpty());
    }

    @Test
    void companyPreferencesInfluenceCareerRecommendationRerun() throws Exception {
        String email = newEmail();
        String password = "recommendation-company-password";
        register(email, password);
        MockHttpSession session = login(email, password);
        mockMvc.perform(post("/api/assessments")
                        .session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "domains":["software"],
                                  "interests":"web development",
                                  "customInterest":"",
                                  "skillLevel":"Beginner",
                                  "goals":["projects"]
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/preferences/companies")
                        .session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyKeys":["microsoft"]}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/assessments/recommendations").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations[0].reason",
                        org.hamcrest.Matchers.containsString("target companies")));
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)))
                .andExpect(status().isCreated());
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String newEmail() {
        return UUID.randomUUID() + "@example.test";
    }
}
