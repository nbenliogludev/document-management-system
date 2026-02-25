package com.nbenliogludev.documentmanagementservice;

import com.nbenliogludev.documentmanagementservice.config.DocumentWorkersProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(DocumentWorkersProperties.class)
public class DocumentManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagementServiceApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
    }
}
