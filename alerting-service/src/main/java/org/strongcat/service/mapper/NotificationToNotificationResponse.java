package org.strongcat.service.mapper;

import org.strongcat.data.Notification;
import org.strongcat.dto.NotificationResponse;

public class NotificationToNotificationResponse {

    public static NotificationResponse convertToResponse(Notification entity) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setEmail(entity.getEmail());
        dto.setServiceName(entity.getServiceName());
        dto.setLevel(entity.getLevel());
        dto.setTextQuery(entity.getTextQuery());
        dto.setThresholdCount(entity.getThresholdCount());
        dto.setPollingIntervalSeconds(entity.getPollingIntervalSeconds());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
