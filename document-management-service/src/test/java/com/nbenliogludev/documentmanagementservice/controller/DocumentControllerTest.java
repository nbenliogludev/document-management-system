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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.transaction.TestTransaction;

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
                                                "Invalid status transition for document " + id
                                                                + ": cannot transition from DRAFT to APPROVED"));
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

        @Test
        void batchSubmit_ShouldReturnPartialSuccess() throws Exception {
                CreateDocumentRequest req = new CreateDocumentRequest();
                req.setTitle("Batch Submit Doc 1");
                req.setAuthor("Author 1");
                String responseStr = mockMvc.perform(post("/api/v1/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andReturn().getResponse().getContentAsString();
                String idStr = objectMapper.readTree(responseStr).get("id").asText();

                TestTransaction.flagForCommit();
                TestTransaction.end();
                TestTransaction.start();

                UUID unknownId = UUID.randomUUID();

                String batchReq = "{\"ids\": [\"" + idStr + "\", \"" + unknownId + "\"]}";

                mockMvc.perform(post("/api/v1/documents/submit/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(batchReq))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.summary.total").value(2))
                                .andExpect(jsonPath("$.summary.ok").value(1))
                                .andExpect(jsonPath("$.summary.failed").value(1))
                                .andExpect(jsonPath("$.results[0].id").value(idStr))
                                .andExpect(jsonPath("$.results[0].status").value("OK"))
                                .andExpect(jsonPath("$.results[1].id").value(unknownId.toString()))
                                .andExpect(jsonPath("$.results[1].status").value("NOT_FOUND"));
        }

        @Test
        void batchApprove_ShouldReturnPartialSuccess() throws Exception {
                CreateDocumentRequest req1 = new CreateDocumentRequest();
                req1.setTitle("Batch Approve Doc 1");
                req1.setAuthor("Author 1");
                String responseStr1 = mockMvc.perform(post("/api/v1/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req1)))
                                .andReturn().getResponse().getContentAsString();
                String idStr1 = objectMapper.readTree(responseStr1).get("id").asText();

                CreateDocumentRequest req2 = new CreateDocumentRequest();
                req2.setTitle("Batch Approve Doc 2");
                req2.setAuthor("Author 2");
                String responseStr2 = mockMvc.perform(post("/api/v1/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req2)))
                                .andReturn().getResponse().getContentAsString();
                String idStr2 = objectMapper.readTree(responseStr2).get("id").asText();

                // Submit Doc 1
                mockMvc.perform(post("/api/v1/documents/{id}/submit", idStr1))
                                .andExpect(status().isOk());

                TestTransaction.flagForCommit();
                TestTransaction.end();
                TestTransaction.start();

                // Batch approve both. Doc 1 is SUBMITTED (will succeed), Doc 2 is DRAFT (will
                // fail)
                String batchReq = "{\"ids\": [\"" + idStr1 + "\", \"" + idStr2 + "\"]}";

                mockMvc.perform(post("/api/v1/documents/approve/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(batchReq))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.summary.total").value(2))
                                .andExpect(jsonPath("$.summary.ok").value(1))
                                .andExpect(jsonPath("$.summary.failed").value(1))
                                .andExpect(jsonPath("$.results[0].id").value(idStr1))
                                .andExpect(jsonPath("$.results[0].status").value("OK"))
                                .andExpect(jsonPath("$.results[1].id").value(idStr2))
                                .andExpect(jsonPath("$.results[1].status").value("INVALID_STATUS"));

                // Test Double Approve through batch
                String batchReq2 = "{\"ids\": [\"" + idStr1 + "\"]}";
                mockMvc.perform(post("/api/v1/documents/approve/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(batchReq2))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.summary.total").value(1))
                                .andExpect(jsonPath("$.summary.ok").value(0))
                                .andExpect(jsonPath("$.summary.failed").value(1))
                                .andExpect(jsonPath("$.results[0].id").value(idStr1))
                                .andExpect(jsonPath("$.results[0].status").value("ALREADY_APPROVED"));
        }

        @Test
        void concurrencyCheck_ShouldAllowOnlyOneApproval() throws Exception {
                CreateDocumentRequest req = new CreateDocumentRequest();
                req.setTitle("Concurrency Doc");
                req.setAuthor("Concurrency Author");
                String responseStr = mockMvc.perform(post("/api/v1/documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andReturn().getResponse().getContentAsString();
                String idStr = objectMapper.readTree(responseStr).get("id").asText();

                // Submit the document DocumentStatus.SUBMITTED
                mockMvc.perform(post("/api/v1/documents/{id}/submit", idStr))
                                .andExpect(status().isOk());

                TestTransaction.flagForCommit();
                TestTransaction.end();

                String concurrencyReq = "{\"threads\": 10, \"attempts\": 30}";

                mockMvc.perform(post("/api/v1/documents/{id}/approve/concurrency-check", idStr)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(concurrencyReq))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.threads").value(10))
                                .andExpect(jsonPath("$.attempts").value(30))
                                .andExpect(jsonPath("$.successCount").value(1))
                                .andExpect(jsonPath("$.conflictCount").value(29))
                                .andExpect(jsonPath("$.errorCount").value(0))
                                .andExpect(jsonPath("$.finalDocumentStatus").value("APPROVED"))
                                .andExpect(jsonPath("$.registryRecordExists").value(true))
                                .andExpect(jsonPath("$.registryRecordCount").value(1));
        }

        @Test
        void concurrencyCheck_ShouldReturn404_WhenDocumentNotFound() throws Exception {
                UUID randomId = UUID.randomUUID();
                String concurrencyReq = "{\"threads\": 5, \"attempts\": 10}";

                mockMvc.perform(post("/api/v1/documents/{id}/approve/concurrency-check", randomId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(concurrencyReq))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Not Found"));
        }
}
