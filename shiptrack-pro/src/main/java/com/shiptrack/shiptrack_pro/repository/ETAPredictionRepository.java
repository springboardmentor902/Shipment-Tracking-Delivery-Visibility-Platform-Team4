package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.ETAPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ETAPredictionRepository
        extends JpaRepository<ETAPrediction, Long> {

    Optional<ETAPrediction> findByShipmentId(Long shipmentId);


    // Used by ReportService — Delay analysis report, shipments above a risk threshold
    List<ETAPrediction> findByDelayRiskScoreGreaterThanEqual(
            Double threshold
    );
}