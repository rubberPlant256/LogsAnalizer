package org.strongcat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.strongcat.data.Notification;
import org.strongcat.dto.CreateUpdateNotificationRequest;
import org.strongcat.dto.NotificationListResponse;
import org.strongcat.dto.NotificationResponse;
import org.strongcat.service.NotificationService;
import org.strongcat.service.mapper.NotificationToNotificationResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<NotificationListResponse> getByUserId(@PathVariable UUID userId) {
        List<Notification> entities = notificationService.getByUserId(userId);

        List<NotificationResponse> notificationDtos = entities.stream()
                .map(NotificationToNotificationResponse::convertToResponse)
                .toList();

        NotificationListResponse response = new NotificationListResponse(
                notificationDtos
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CreateUpdateNotificationRequest request) {
        Notification created = notificationService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationToNotificationResponse.convertToResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateUpdateNotificationRequest request) {

        Notification updated = notificationService.update(id, request);
        return ResponseEntity.ok(NotificationToNotificationResponse.convertToResponse(updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(
            @PathVariable Long id, 
            @RequestParam boolean isActive) {

        notificationService.toggleStatus(id, isActive);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}