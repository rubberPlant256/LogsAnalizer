package org.strongcat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.strongcat.data.EmailAlertContext;
import org.strongcat.data.Notification;
import org.strongcat.dto.CreateUpdateNotificationRequest;
import org.strongcat.exception.ResourceNotFoundCustomException;
import org.strongcat.exception.ValidationCustomException;
import org.strongcat.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailSenderService emailSenderService;

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

        EmailAlertContext alertContext = new EmailAlertContext(
                notification.getEmail(),
                notification.getServiceName(),
                notification.getLevel(),
                notification.getTextQuery(),
                7,
                notification.getThresholdCount()
        );

        emailSenderService.sendAlert(alertContext);

        return notificationRepository.save(notification);
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

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification toggleStatus(Long id, boolean isActive) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setIsActive(isActive);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }
}