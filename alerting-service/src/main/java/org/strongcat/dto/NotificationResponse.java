package org.strongcat.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationResponse {
    private Long id;
    private UUID userId;
    private String email;
    private String serviceName;
    private String level;
    private String textQuery;
    private Integer thresholdCount;
    private Integer pollingIntervalSeconds;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}