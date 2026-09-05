package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByShipmentIdAndType(Long shipmentId, String type);

    Optional<Notification> findTopByShipmentIdAndTypeOrderByCreatedAtDesc(Long shipmentId, String type);
}