package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.Route;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private Long id;
    private Long shipmentId;
    private String originAddress;
    private String destinationAddress;
    private Double originLat;
    private Double originLng;
    private Double destinationLat;
    private Double destinationLng;
    private Double currentLat;
    private Double currentLng;
    private Double distanceKm;
    private Integer estimatedDurationMinutes;
    private Integer actualDurationMinutes;
    private LocalDateTime eta;
    private LocalDateTime actualDeliveryTime;
    private String status;
    private Long assignedDriverId;
    private String driverName;
    private String driverPhone;
    private String vehicleNumber;
    private String routePolyline;
    private String waypoints;
    private Integer trafficDelayMinutes;
    private Boolean isCurrentRoute;
    private String notes;
    private Long createdByUserId;
    private Long updatedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RouteResponse fromEntity(Route route) {
        if (route == null) return null;

        return RouteResponse.builder()
                .id(route.getId())
                .shipmentId(route.getShipmentId())
                .originAddress(route.getOriginAddress())
                .destinationAddress(route.getDestinationAddress())
                .originLat(route.getOriginLat())
                .originLng(route.getOriginLng())
                .destinationLat(route.getDestinationLat())
                .destinationLng(route.getDestinationLng())
                .currentLat(route.getCurrentLat())
                .currentLng(route.getCurrentLng())
                .distanceKm(route.getDistanceKm())
                .estimatedDurationMinutes(route.getEstimatedDurationMinutes())
                .actualDurationMinutes(route.getActualDurationMinutes())
                .eta(route.getEta())
                .actualDeliveryTime(route.getActualDeliveryTime())
                .status(route.getStatus())
                .assignedDriverId(route.getAssignedDriverId())
                .driverName(route.getDriverName())
                .driverPhone(route.getDriverPhone())
                .vehicleNumber(route.getVehicleNumber())
                .routePolyline(route.getRoutePolyline())
                .waypoints(route.getWaypoints())
                .trafficDelayMinutes(route.getTrafficDelayMinutes())
                .isCurrentRoute(route.getIsCurrentRoute())
                .notes(route.getNotes())
                .createdByUserId(route.getCreatedByUserId())
                .updatedByUserId(route.getUpdatedByUserId())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }
}