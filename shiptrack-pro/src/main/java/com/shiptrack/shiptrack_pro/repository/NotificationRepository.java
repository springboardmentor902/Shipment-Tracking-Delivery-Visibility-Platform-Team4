package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get notifications for a specific logged-in user,
    // newest notifications first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Used when marking a notification as read
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}