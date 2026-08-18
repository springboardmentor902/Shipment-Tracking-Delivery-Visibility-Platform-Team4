package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository
        extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(
            String trackingNumber
    );

    boolean existsByTrackingNumber(
            String trackingNumber
    );

    List<Shipment> findByUserId(
            Long userId
    );

    Optional<Shipment> findByIdAndUserId(
            Long id,
            Long userId
    );
}