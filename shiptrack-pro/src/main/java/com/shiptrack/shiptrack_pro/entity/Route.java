package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "origin")
    private String origin;

    @Column(name = "destination")
    private String destination;

    @Column(name = "waypoints", columnDefinition = "TEXT")
    private String waypoints;  //intermediate point

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "actual_time_minutes")
    private Integer actualTimeMinutes;

    @Column(name = "traffic_condition")
    private String trafficCondition; //not yet implemented

    @CreationTimestamp  //a Hibernate-specific annotation
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}