package org.strongcat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Getter
@Setter
public class CreateUpdateNotificationRequest {

    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    private String serviceName;

    private String level;

    private String textQuery;

    @NotNull(message = "Threshold count is required")
    @Min(value = 1, message = "Threshold count must be at least 1")
    private Integer thresholdCount;

    @NotNull(message = "Polling interval is required")
    @Min(value = 1, message = "Polling interval must be greater than 0")
    private Integer pollingIntervalSeconds;
}