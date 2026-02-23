package com.nbenliogludev.documentgenerator.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbenliogludev.documentgenerator.config.GeneratorConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DocumentApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String endpointUrl;

    public DocumentApiClient(GeneratorConfig config) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();

        // Ensure standard formatting without trailing slashes
        String baseUrl = config.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.endpointUrl = baseUrl + "/api/v1/documents";
    }

    /**
     * Sends a POST request to create a document.
     * 
     * @param title  the document title
     * @param author the document author
     * @return true if successful, false otherwise
     */
    public boolean createDocument(String title, String author) {
        try {
            Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put("title", title);
            payloadMap.put("author", author);

            String requestBody = objectMapper.writeValueAsString(payloadMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return true;
            } else {
                System.err.println(
                        String.format("[generator] API Error: HTTP %d - %s", response.statusCode(), response.body()));
                return false;
            }

        } catch (Exception e) {
            System.err.println("[generator] Request failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
