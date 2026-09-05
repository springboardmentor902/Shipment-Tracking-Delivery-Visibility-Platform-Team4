package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
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


    // Customer / Business Client
    List<Shipment> findByUserId(
            Long userId
    );


    Optional<Shipment> findByIdAndUserId(
            Long id,
            Long userId
    );


    // Logistics Operator
    List<Shipment> findByAssignedOperatorId(
            Long operatorId
    );


    Optional<Shipment> findByIdAndAssignedOperatorId(
            Long id,
            Long operatorId
    );


    // Used by ETASchedulerService to find active shipments
    List<Shipment> findByStatusIn(
            List<ShipmentStatus> statuses
    );
}