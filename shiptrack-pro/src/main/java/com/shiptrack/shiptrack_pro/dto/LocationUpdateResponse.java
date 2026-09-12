package com.shiptrack.shiptrack_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateResponse {

    private Long routeId;
    private Long shipmentId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;
    private String locationName;
    private String status;
    private LocalDateTime timestamp;
    private Double estimatedArrivalTime; // ETA in minutes
    private Double distanceToDestination; // Remaining distance in km
    private String routePolyline;

    public static LocationUpdateResponse fromLocation(LocationUpdateRequest request,
                                                      Long shipmentId,
                                                      String status,
                                                      Double distanceToDestination,
                                                      Double etaMinutes,
                                                      String routePolyline) {
        return LocationUpdateResponse.builder()
                .routeId(request.getRouteId())
                .shipmentId(shipmentId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(request.getSpeed())
                .heading(request.getHeading())
                .locationName(request.getLocationName())
                .status(status)
                .timestamp(LocalDateTime.now())
                .distanceToDestination(distanceToDestination)
                .estimatedArrivalTime(etaMinutes)
                .routePolyline(routePolyline)
                .build();
    }
}