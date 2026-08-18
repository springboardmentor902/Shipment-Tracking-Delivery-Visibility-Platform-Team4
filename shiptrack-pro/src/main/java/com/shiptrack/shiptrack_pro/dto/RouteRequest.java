package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RouteRequest {

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    private String routeName;

    private Double distanceKm;

    private Integer estimatedDurationMinutes;
}