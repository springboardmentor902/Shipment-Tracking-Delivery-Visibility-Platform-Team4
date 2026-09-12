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
     * Driver sends current location.
     */
    @PostMapping("/route/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'DRIVER', 'ADMINISTRATOR')")
    public ResponseEntity<LocationUpdateResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request) {

        log.info("Updating location for route: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found with id: " + id
                ));

        Shipment shipment = route.getShipment();

        if (shipment == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found for route: " + id
            );
        }

        /*
         * The current Route entity does not store latitude/longitude.
         * Therefore the current location is stored on Shipment.
         */

        String locationName = request.getLocationName();

        if (locationName == null || locationName.isBlank()) {
            try {
                OSMService.GeocodeResult geocodeResult =
                        osmService.reverseGeocode(
                                request.getLatitude(),
                                request.getLongitude()
                        );

                if (geocodeResult != null) {
                    locationName = geocodeResult.getFormattedAddress();
                }

            } catch (Exception e) {
                log.warn("Reverse geocoding failed: {}", e.getMessage());
            }
        }

        if (locationName != null && !locationName.isBlank()) {
            shipment.setCurrentLocation(locationName);
        } else {
            shipment.setCurrentLocation(
                    "Lat: " + request.getLatitude()
                            + ", Lng: " + request.getLongitude()
            );
        }

        shipmentRepository.save(shipment);

        String status = shipment.getStatus() != null
                ? shipment.getStatus().toString()
                : "IN_TRANSIT";

        LocationUpdateResponse response =
                LocationUpdateResponse.fromLocation(
                        request,
                        shipment.getId(),
                        status,
                        0.0,
                        0.0,
                        null
                );

        broadcastService.broadcastLocation(
                shipment.getId(),
                response
        );

        log.info(
                "Location updated for shipment {}: {}",
                shipment.getId(),
                shipment.getCurrentLocation()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * WebSocket endpoint for real-time location updates.
     */
    @MessageMapping("/location")
    @SendTo("/topic/location")
    public LocationUpdateResponse handleLocationUpdate(
            @Payload LocationUpdateRequest request) {

        log.info(
                "Received location update via WebSocket for route: {}",
                request.getRouteId()
        );

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found"
                ));

        Shipment shipment = route.getShipment();

        if (shipment == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found"
            );
        }

        String locationName = request.getLocationName();

        if (locationName == null || locationName.isBlank()) {
            try {
                OSMService.GeocodeResult geocodeResult =
                        osmService.reverseGeocode(
                                request.getLatitude(),
                                request.getLongitude()
                        );

                if (geocodeResult != null) {
                    locationName = geocodeResult.getFormattedAddress();
                }

            } catch (Exception e) {
                log.warn(
                        "Reverse geocoding failed: {}",
                        e.getMessage()
                );
            }
        }

        if (locationName != null && !locationName.isBlank()) {
            shipment.setCurrentLocation(locationName);
        } else {
            shipment.setCurrentLocation(
                    "Lat: " + request.getLatitude()
                            + ", Lng: " + request.getLongitude()
            );
        }

        shipmentRepository.save(shipment);

        String status = shipment.getStatus() != null
                ? shipment.getStatus().toString()
                : "IN_TRANSIT";

        LocationUpdateResponse response =
                LocationUpdateResponse.fromLocation(
                        request,
                        shipment.getId(),
                        status,
                        0.0,
                        0.0,
                        null
                );

        broadcastService.broadcastLocation(
                shipment.getId(),
                response
        );

        return response;
    }

    /**
     * GET /api/location/shipment/{shipmentId}
     * Gets the latest route associated with a shipment.
     */
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<LocationUpdateResponse> getLastKnownLocation(
            @PathVariable Long shipmentId) {

        log.info(
                "Getting last known location for shipment: {}",
                shipmentId
        );

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found: " + shipmentId
                ));

        var routes = routeRepository
                .findByShipmentIdOrderByCreatedAtDesc(shipmentId);

        if (routes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Route not found for shipment: " + shipmentId
            );
        }

        Route route = routes.get(0);

        String status = shipment.getStatus() != null
                ? shipment.getStatus().toString()
                : "IN_TRANSIT";

        LocationUpdateResponse response =
                LocationUpdateResponse.builder()
                        .routeId(route.getId())
                        .shipmentId(shipment.getId())
                        .latitude(null)
                        .longitude(null)
                        .status(status)
                        .timestamp(LocalDateTime.now())
                        .distanceToDestination(0.0)
                        .estimatedArrivalTime(0.0)
                        .routePolyline(null)
                        .locationName(shipment.getCurrentLocation())
                        .build();

        return ResponseEntity.ok(response);
    }
}