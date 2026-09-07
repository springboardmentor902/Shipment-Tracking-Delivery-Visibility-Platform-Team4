package com.shiptrack.shiptrack_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAlternativeDTO {

    private Long routeId;
    private Double distanceKm;
    private Integer durationMinutes;
    private Integer trafficDurationMinutes;
    private String summary;
    private String polyline;
    private String originAddress;
    private String destinationAddress;
    private String routeType; // "Fastest", "Shortest", "Balanced"
}