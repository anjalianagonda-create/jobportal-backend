package com.example.jobportal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** End-to-end walk through the main business flow: register, post, search, apply, shortlist. */
@SpringBootTest
@ActiveProfiles("test")
class RecruitmentFlowTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void fullRecruitmentFlow() throws Exception {
        String employerToken = token(register("Eva Employer", "eva@acme.test", "EMPLOYER", "Acme Corp"));
        String seekerToken = token(register("Sam Seeker", "sam@mail.test", "JOB_SEEKER", null));

        // Employer posts a job
        String deadline = LocalDate.now().plusDays(30).toString();
        String jobJson = """
                {"title":"Java Developer","description":"Build Spring Boot services","location":"Hyderabad",
                 "jobType":"FULL_TIME","workMode":"HYBRID","minExperience":1,"skills":["Java","Spring Boot"],
                 "openings":1,"deadline":"%s"}""".formatted(deadline);
        String created = send(post("/api/employer/jobs"), employerToken, jobJson, 201);
        String jobId = firstNumber(created, "id");

        // Public smart search finds it (no token needed)
        String search = send(get("/api/jobs?keyword=spring&location=hyder&experience=2"), null, null, 200);
        assertTrue(search.contains("Java Developer"));
        String noMatch = send(get("/api/jobs?keyword=spring&experience=0"), null, null, 200);
        assertTrue(noMatch.contains("\"totalElements\":0"));

        // Role based access control
        send(get("/api/employer/jobs"), seekerToken, null, 403);
        send(get("/api/seeker/applications"), null, null, 401);

        // Seeker applies once; a second attempt is rejected
        send(post("/api/seeker/jobs/" + jobId + "/apply"), seekerToken, "{\"coverLetter\":\"Hello\"}", 201);
        send(post("/api/seeker/jobs/" + jobId + "/apply"), seekerToken, "{}", 409);

        // Employer reviews and shortlists
        String applicants = send(get("/api/employer/jobs/" + jobId + "/applications"), employerToken, null, 200);
        assertTrue(applicants.contains("Sam Seeker"));
        String applicationId = firstNumber(applicants, "id");
        send(patch("/api/employer/applications/" + applicationId + "/status"), employerToken,
                "{\"status\":\"SHORTLISTED\",\"note\":\"Strong profile\"}", 200);

        // Seeker sees the updated status
        String mine = send(get("/api/seeker/applications"), seekerToken, null, 200);
        assertTrue(mine.contains("SHORTLISTED"));
    }

    @Test
    void duplicateEmailAndInvalidInputAreRejected() throws Exception {
        register("Dup User", "dup@mail.test", "JOB_SEEKER", null);
        send(post("/api/auth/register"), null,
                "{\"fullName\":\"Dup\",\"email\":\"dup@mail.test\",\"password\":\"password1\",\"role\":\"JOB_SEEKER\"}", 409);
        send(post("/api/auth/register"), null,
                "{\"fullName\":\"\",\"email\":\"bad\",\"password\":\"x\",\"role\":\"JOB_SEEKER\"}", 400);
        send(post("/api/auth/login"), null, "{\"email\":\"dup@mail.test\",\"password\":\"wrong-pass\"}", 401);
    }

    // ------------------------------------------------------------------ helpers

    private String register(String name, String email, String role, String company) throws Exception {
        String companyField = company == null ? "" : ",\"companyName\":\"" + company + "\"";
        String body = "{\"fullName\":\"" + name + "\",\"email\":\"" + email
                + "\",\"password\":\"password123\",\"role\":\"" + role + "\"" + companyField + "}";
        return send(post("/api/auth/register"), null, body, 201);
    }

    private String send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
                        String token, String body, int expectedStatus) throws Exception {
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        var response = mvc.perform(builder).andReturn().getResponse();
        assertEquals(expectedStatus, response.getStatus(), "Unexpected status. Body: " + response.getContentAsString());
        return response.getContentAsString();
    }

    private static String token(String json) {
        return firstString(json, "token");
    }

    private static String firstString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":\"([^\"]+)\"").matcher(json);
        assertTrue(m.find(), "Missing " + key + " in " + json);
        return m.group(1);
    }

    private static String firstNumber(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":(\\d+)").matcher(json);
        assertTrue(m.find(), "Missing " + key + " in " + json);
        return m.group(1);
    }
}
