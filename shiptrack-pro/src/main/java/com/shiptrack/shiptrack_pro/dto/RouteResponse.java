package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RouteResponse {

    private Long id;

    private Long shipmentId;

    private String trackingNumber;

    private String origin;

    private String destination;

    private String routeName;

    private Double distanceKm;

    private Integer estimatedDurationMinutes;

    private String assignedBy;

    private LocalDateTime createdAt;
}