package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.LocationUpdateRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateResponse;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.service.LocationBroadcastService;
import com.shiptrack.shiptrack_pro.service.OSMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final LocationBroadcastService broadcastService;
    private final OSMService osmService;

    /**
     * POST /api/location/route/{id}
     * Driver sends current location
     */
    @PostMapping("/route/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'DRIVER', 'ADMINISTRATOR')")
    public ResponseEntity<LocationUpdateResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request) {

        log.info("📍 Updating location for route: {}", id);

        // Get the route
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found with id: " + id
                ));

        // Get the shipment
        Shipment shipment = shipmentRepository.findById(route.getShipmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found for route: " + id
                ));

        // ✅ UPDATE ROUTE current location
        route.setCurrentLat(request.getLatitude());
        route.setCurrentLng(request.getLongitude());

        if (route.getStatus() == null || "PLANNED".equals(route.getStatus())) {
            route.setStatus("IN_TRANSIT");
        }

        // ✅ UPDATE SHIPMENT current location
        String locationName = request.getLocationName();
        if (locationName == null || locationName.isEmpty()) {
            // Try to reverse geocode if no location name provided
            try {
                OSMService.GeocodeResult geocodeResult = osmService.reverseGeocode(
                        request.getLatitude(), request.getLongitude()
                );
                if (geocodeResult != null) {
                    locationName = geocodeResult.getFormattedAddress();
                }
            } catch (Exception e) {
                log.warn("Reverse geocoding failed: {}", e.getMessage());
            }
        }

        if (locationName != null && !locationName.isEmpty()) {
            shipment.setCurrentLocation(locationName);
        } else {
            shipment.setCurrentLocation(
                    "Lat: " + request.getLatitude() + ", Lng: " + request.getLongitude()
            );
        }

        // Calculate remaining distance and ETA
        double remainingDistance = 0;
        int etaMinutes = 0;

        if (route.getDestinationLat() != null && route.getDestinationLng() != null) {
            OSMService.DistanceResult distanceResult = osmService.calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    route.getDestinationLat(), route.getDestinationLng()
            );

            if (distanceResult != null) {
                remainingDistance = distanceResult.getDistanceKm();
                etaMinutes = distanceResult.getDurationMinutes();
                route.setEta(LocalDateTime.now().plusMinutes(etaMinutes + 30));
            }
        }

        // ✅ Save both route and shipment
        routeRepository.save(route);
        shipmentRepository.save(shipment);

        log.info("✅ Route updated: lat={}, lng={}", request.getLatitude(), request.getLongitude());
        log.info("✅ Shipment location updated: {}", shipment.getCurrentLocation());

        // Build response
        LocationUpdateResponse response = LocationUpdateResponse.fromLocation(
                request,
                shipment.getId(),
                route.getStatus(),
                remainingDistance,
                (double) etaMinutes,
                route.getRoutePolyline()
        );

        // Broadcast to all subscribers
        broadcastService.broadcastLocation(shipment.getId(), response);

        return ResponseEntity.ok(response);
    }

    /**
     * WebSocket endpoint for real-time location updates
     */
    @MessageMapping("/location")
    @SendTo("/topic/location")
    public LocationUpdateResponse handleLocationUpdate(@Payload LocationUpdateRequest request) {
        log.info("📡 Received location update via WebSocket for route: {}", request.getRouteId());

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found"
                ));

        Shipment shipment = shipmentRepository.findById(route.getShipmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                ));

        // Update route
        route.setCurrentLat(request.getLatitude());
        route.setCurrentLng(request.getLongitude());

        if (route.getStatus() == null || "PLANNED".equals(route.getStatus())) {
            route.setStatus("IN_TRANSIT");
        }

        // Update shipment
        String locationName = request.getLocationName();
        if (locationName != null && !locationName.isEmpty()) {
            shipment.setCurrentLocation(locationName);
        } else {
            shipment.setCurrentLocation("Lat: " + request.getLatitude() + ", Lng: " + request.getLongitude());
        }

        routeRepository.save(route);
        shipmentRepository.save(shipment);

        double remainingDistance = 0;
        int etaMinutes = 0;
        if (route.getDestinationLat() != null && route.getDestinationLng() != null) {
            OSMService.DistanceResult distanceResult = osmService.calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    route.getDestinationLat(), route.getDestinationLng()
            );
            if (distanceResult != null) {
                remainingDistance = distanceResult.getDistanceKm();
                etaMinutes = distanceResult.getDurationMinutes();
            }
        }

        LocationUpdateResponse response = LocationUpdateResponse.fromLocation(
                request,
                shipment.getId(),
                route.getStatus(),
                remainingDistance,
                (double) etaMinutes,
                route.getRoutePolyline()
        );

        // Broadcast to shipment channel
        broadcastService.broadcastLocation(shipment.getId(), response);

        return response;
    }

    /**
     * GET /api/location/shipment/{shipmentId}
     */
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<LocationUpdateResponse> getLastKnownLocation(
            @PathVariable Long shipmentId) {

        log.info("📍 Getting last known location for shipment: {}", shipmentId);

        Route route = routeRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found for shipment: " + shipmentId
                ));

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found: " + shipmentId
                ));

        double remainingDistance = 0;
        int etaMinutes = 0;

        if (route.getCurrentLat() != null && route.getCurrentLng() != null &&
                route.getDestinationLat() != null && route.getDestinationLng() != null) {

            OSMService.DistanceResult distanceResult = osmService.calculateDistance(
                    route.getCurrentLat(), route.getCurrentLng(),
                    route.getDestinationLat(), route.getDestinationLng()
            );

            if (distanceResult != null) {
                remainingDistance = distanceResult.getDistanceKm();
                etaMinutes = distanceResult.getDurationMinutes();
            }
        }

        LocationUpdateResponse response = LocationUpdateResponse.builder()
                .routeId(route.getId())
                .shipmentId(shipment.getId())
                .latitude(route.getCurrentLat() != null ? route.getCurrentLat() : route.getOriginLat())
                .longitude(route.getCurrentLng() != null ? route.getCurrentLng() : route.getOriginLng())
                .status(route.getStatus())
                .timestamp(LocalDateTime.now())
                .distanceToDestination(remainingDistance)
                .estimatedArrivalTime((double) etaMinutes)
                .routePolyline(route.getRoutePolyline())
                .locationName(shipment.getCurrentLocation())
                .build();

        return ResponseEntity.ok(response);
    }
}