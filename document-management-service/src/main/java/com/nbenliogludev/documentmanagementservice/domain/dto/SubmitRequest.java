package com.nbenliogludev.documentmanagementservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequest {

    @NotBlank(message = "Initiator is required")
    private String initiator;

    private String comment;
}
