package com.whistledrop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whistledrop.dto.CreateReportRequest;
import com.whistledrop.dto.LoginRequest;
import com.whistledrop.dto.StatusUpdateRequest;
import com.whistledrop.entity.ReportCategory;
import com.whistledrop.entity.ReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WhistleDropApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to obtain a valid moderator JWT token
    private String obtainModeratorToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("moderator", "password");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseNode.get("token").asText();
    }

    // Helper to create a report and return the case code
    private String createReportAndGetCaseCode(ReportCategory category, String description) throws Exception {
        CreateReportRequest request = new CreateReportRequest(category, description, null);
        MvcResult result = mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseNode.get("caseCode").asText();
    }

    // 1. Report creation
    @Test
    @DisplayName("1. Report creation - anonymous submission should return 201 and case code")
    void testCreateReport() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
                ReportCategory.SECURITY,
                "Unauthorized access attempt detected on system server.",
                "https://example.com/evidence/log1"
        );

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Report submitted successfully."))
                .andExpect(jsonPath("$.caseCode", startsWith("WD-")))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    // 2. Invalid category
    @Test
    @DisplayName("2. Invalid category - should return 400 Bad Request")
    void testInvalidCategory() throws Exception {
        String invalidJson = """
                {
                    "category": "INVALID_CATEGORY",
                    "description": "Some report content"
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    // 3. Empty description
    @Test
    @DisplayName("3. Empty description - should fail validation with 400 Bad Request")
    void testEmptyDescription() throws Exception {
        CreateReportRequest request = new CreateReportRequest(ReportCategory.CORRUPTION, "", null);

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Description is required")));
    }

    // 4. Case-code tracking
    @Test
    @DisplayName("4. Case-code tracking - anonymous user can view status and update timeline")
    void testTrackReportByCaseCode() throws Exception {
        String caseCode = createReportAndGetCaseCode(
                ReportCategory.HARASSMENT,
                "Harassment issue reported in the computer science department."
        );

        mockMvc.perform(get("/api/reports/" + caseCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseCode").value(caseCode))
                .andExpect(jsonPath("$.category").value("HARASSMENT"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updates", hasSize(1)))
                .andExpect(jsonPath("$.updates[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$.updates[0].message").value("Report received."))
                // Privacy check: internal ID must not be exposed to anonymous user
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    // 5. Invalid case code
    @Test
    @DisplayName("5. Invalid case code - tracking with non-existent code returns 404")
    void testInvalidCaseCode() throws Exception {
        mockMvc.perform(get("/api/reports/WD-NONEXISTENT123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Report not found for the provided case code."));
    }

    // 6. Moderator login
    @Test
    @DisplayName("6. Moderator login - valid credentials return JWT token, invalid credentials return 401")
    void testModeratorLogin() throws Exception {
        // Valid login
        LoginRequest validRequest = new LoginRequest("moderator", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));

        // Invalid login
        LoginRequest invalidRequest = new LoginRequest("moderator", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    // 7. Unauthorized moderator endpoint
    @Test
    @DisplayName("7. Unauthorized moderator endpoint - accessing without JWT returns 401")
    void testUnauthorizedModeratorEndpoint() throws Exception {
        mockMvc.perform(get("/api/moderator/reports"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    // 8. Valid status transition
    @Test
    @DisplayName("8. Valid status transition - SUBMITTED -> UNDER_REVIEW -> RESOLVED")
    void testValidStatusTransition() throws Exception {
        String token = obtainModeratorToken();

        // 1. Submit report
        CreateReportRequest createRequest = new CreateReportRequest(
                ReportCategory.TECHNICAL,
                "SQL injection vulnerability identified in public student portal.",
                null
        );
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // 2. Moderator views reports to find the report id
        MvcResult listResult = mockMvc.perform(get("/api/moderator/reports")
                        .header("Authorization", "Bearer " + token)
                        .param("category", "TECHNICAL"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode reportsNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
        String reportId = reportsNode.get(0).get("id").asText();

        // 3. Transition: SUBMITTED -> UNDER_REVIEW
        StatusUpdateRequest toUnderReview = new StatusUpdateRequest(
                ReportStatus.UNDER_REVIEW,
                "Security team is currently investigating the vulnerability."
        );
        mockMvc.perform(patch("/api/moderator/reports/" + reportId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toUnderReview)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.updates", hasSize(2)));

        // 4. Transition: UNDER_REVIEW -> RESOLVED
        StatusUpdateRequest toResolved = new StatusUpdateRequest(
                ReportStatus.RESOLVED,
                "Patch deployed and vulnerability has been successfully resolved."
        );
        mockMvc.perform(patch("/api/moderator/reports/" + reportId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toResolved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.updates", hasSize(3)));
    }

    // 9. Invalid status transition
    @Test
    @DisplayName("9. Invalid status transition - skipping UNDER_REVIEW directly to RESOLVED must be rejected")
    void testInvalidStatusTransition() throws Exception {
        String token = obtainModeratorToken();

        // 1. Submit report (initial status: SUBMITTED)
        CreateReportRequest createRequest = new CreateReportRequest(
                ReportCategory.OTHER,
                "Dispute regarding student council equipment storage.",
                null
        );
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // 2. Find report id
        MvcResult listResult = mockMvc.perform(get("/api/moderator/reports")
                        .header("Authorization", "Bearer " + token)
                        .param("category", "OTHER"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode reportsNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
        String reportId = reportsNode.get(0).get("id").asText();

        // 3. Attempt direct invalid transition: SUBMITTED -> RESOLVED (must be rejected)
        StatusUpdateRequest invalidTransition = new StatusUpdateRequest(
                ReportStatus.RESOLVED,
                "Attempting to resolve without review."
        );

        mockMvc.perform(patch("/api/moderator/reports/" + reportId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTransition)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid status transition")));
    }
}
