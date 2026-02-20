package com.nbenliogludev.documentmanagementservice.controller;

import com.nbenliogludev.documentmanagementservice.domain.dto.CreateDocumentRequest;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class DocumentControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void createDocument_ShouldReturn201_WhenValidRequest() throws Exception {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setTitle("My First Doc");
        request.setAuthor("John Doe");

        mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.number", notNullValue()))
                .andExpect(jsonPath("$.title").value("My First Doc"))
                .andExpect(jsonPath("$.author").value("John Doe"))
                .andExpect(jsonPath("$.status").value(DocumentStatus.DRAFT.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void getById_ShouldReturn404_WhenDocumentDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/documents/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void submitDocument_ShouldReturn200_WhenDraft() throws Exception {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setTitle("Draft Doc");
        request.setAuthor("Test Author");

        String responseStr = mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String idStr = objectMapper.readTree(responseStr).get("id").asText();
        UUID id = UUID.fromString(idStr);

        mockMvc.perform(post("/api/v1/documents/{id}/submit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(DocumentStatus.SUBMITTED.name()));
    }

    @Test
    void approveDocument_ShouldReturn200_WhenSubmitted() throws Exception {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setTitle("Submit Doc");
        request.setAuthor("Test Author");

        String responseStr = mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String idStr = objectMapper.readTree(responseStr).get("id").asText();
        UUID id = UUID.fromString(idStr);

        mockMvc.perform(post("/api/v1/documents/{id}/submit", id))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/documents/{id}/approve", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(DocumentStatus.APPROVED.name()));
    }

    @Test
    void approveDocument_ShouldReturn409_WhenDraft() throws Exception {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setTitle("Error Doc");
        request.setAuthor("Test Author");

        String responseStr = mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String idStr = objectMapper.readTree(responseStr).get("id").asText();
        UUID id = UUID.fromString(idStr);

        mockMvc.perform(post("/api/v1/documents/{id}/approve", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid status transition for document " + id + ": cannot transition from DRAFT to APPROVED"));
    }

    @Test
    void approveDocument_ShouldReturn409_WhenAlreadyApproved() throws Exception {
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setTitle("Double Approve Doc");
        request.setAuthor("Test Author");

        String responseStr = mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String idStr = objectMapper.readTree(responseStr).get("id").asText();
        UUID id = UUID.fromString(idStr);

        mockMvc.perform(post("/api/v1/documents/{id}/submit", id))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/documents/{id}/approve", id))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/documents/{id}/approve", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Document " + id + " is already approved."));
    }
}
