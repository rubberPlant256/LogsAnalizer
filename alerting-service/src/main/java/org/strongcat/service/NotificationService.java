package org.strongcat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.strongcat.data.Notification;
import org.strongcat.dto.CreateUpdateNotificationRequest;
import org.strongcat.event.NotificationChangedEvent;
import org.strongcat.exception.ResourceNotFoundCustomException;
import org.strongcat.exception.ValidationCustomException;
import org.strongcat.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<Notification> getByUserId(UUID userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Transactional
    public Notification create(CreateUpdateNotificationRequest request) {

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setEmail(request.getEmail());
        notification.setServiceName(request.getServiceName());
        notification.setLevel(request.getLevel());
        notification.setTextQuery(request.getTextQuery());
        notification.setThresholdCount(request.getThresholdCount());
        notification.setPollingIntervalSeconds(request.getPollingIntervalSeconds());
        notification.setIsActive(true);

        Notification created = notificationRepository.save(notification);
        eventPublisher.publishEvent(new NotificationChangedEvent());

        return created;
    }

    @Transactional
    public Notification update(Long id, CreateUpdateNotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundCustomException("Notification not found with id: " + id));

        if (!request.getUserId().equals(notification.getUserId())) {
            throw new ValidationCustomException("User id mismatch, request id= " + request.getUserId() +
                    ", db id= " + notification.getUserId());
        }

        notification.setEmail(request.getEmail());
        notification.setServiceName(request.getServiceName());
        notification.setLevel(request.getLevel());
        notification.setTextQuery(request.getTextQuery());
        notification.setThresholdCount(request.getThresholdCount());
        notification.setPollingIntervalSeconds(request.getPollingIntervalSeconds());

        Notification updated = notificationRepository.save(notification);
        eventPublisher.publishEvent(new NotificationChangedEvent());

        return updated;
    }

    @Transactional
    public void toggleStatus(Long id, boolean isActive) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setIsActive(isActive);
        notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationChangedEvent());
    }

    @Transactional
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found with id: " + id);
        }

        notificationRepository.deleteById(id);
        eventPublisher.publishEvent(new NotificationChangedEvent());
    }

    public List<Notification> findActiveRulesByInterval(Integer intervalSeconds){
        return notificationRepository.findByPollingIntervalSecondsAndIsActiveTrue(intervalSeconds);
    }


    public List<Notification> findAllActiveRules() {
        return notificationRepository.findAllByIsActiveTrue();
    }
}