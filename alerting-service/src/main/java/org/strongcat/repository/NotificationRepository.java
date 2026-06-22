package org.strongcat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.strongcat.data.Notification;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(UUID userId);

    List<Notification> findByPollingIntervalSecondsAndIsActiveTrue(Integer pollingIntervalSeconds);

    List<Notification> findAllByIsActiveTrue();
}