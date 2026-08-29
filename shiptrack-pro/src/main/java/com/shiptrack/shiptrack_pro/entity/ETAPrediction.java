package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eta_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ETAPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "predicted_delivery_time", nullable = false)
    private LocalDateTime predictedDeliveryTime;

    @Column(name = "delay_risk_score", nullable = false)
    private Double delayRiskScore;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "factors", columnDefinition = "TEXT")
    private String factors;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}