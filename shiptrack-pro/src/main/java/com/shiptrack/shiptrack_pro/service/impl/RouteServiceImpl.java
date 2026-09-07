package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.dto.RouteUpdateRequest;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.service.OSMService;
import com.shiptrack.shiptrack_pro.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final OSMService osmService;

    // ==================== CREATE ====================

    @Override
    @Transactional
    public RouteResponse createRoute(RouteRequest request, Long userId) {
        log.info("Creating route for shipment: {}", request.getShipmentId());

        // Check if shipment exists
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + request.getShipmentId()
                ));

        // Set all existing routes for this shipment to NOT current
        List<Route> existingRoutes = routeRepository.findByShipmentIdOrderByCreatedAtDesc(request.getShipmentId());
        for (Route existingRoute : existingRoutes) {
            existingRoute.setIsCurrentRoute(false);
            routeRepository.save(existingRoute);
        }

        // Get route details from OSM
        OSMService.GeocodeResult originGeocode = osmService.geocodeAddress(request.getOriginAddress());
        OSMService.GeocodeResult destGeocode = osmService.geocodeAddress(request.getDestinationAddress());
        OSMService.DistanceResult distanceResult = null;

        if (originGeocode != null && destGeocode != null) {
            distanceResult = osmService.calculateDistance(
                    originGeocode.getLat(), originGeocode.getLng(),
                    destGeocode.getLat(), destGeocode.getLng()
            );
        }

        // Build route entity
        Route route = Route.builder()
                .shipmentId(request.getShipmentId())
                .originAddress(request.getOriginAddress())
                .destinationAddress(request.getDestinationAddress())
                .originLat(originGeocode != null ? originGeocode.getLat() : null)
                .originLng(originGeocode != null ? originGeocode.getLng() : null)
                .destinationLat(destGeocode != null ? destGeocode.getLat() : null)
                .destinationLng(destGeocode != null ? destGeocode.getLng() : null)
                .currentLat(originGeocode != null ? originGeocode.getLat() : null)
                .currentLng(originGeocode != null ? originGeocode.getLng() : null)
                .distanceKm(distanceResult != null ? distanceResult.getDistanceKm() : null)
                .estimatedDurationMinutes(distanceResult != null ? distanceResult.getDurationMinutes() : null)
                .eta(distanceResult != null ?
                        LocalDateTime.now().plusMinutes(distanceResult.getDurationMinutes() + 30) :
                        LocalDateTime.now().plusDays(1))
                .status("PLANNED")
                .assignedDriverId(request.getAssignedDriverId())
                .driverName(request.getDriverName())
                .driverPhone(request.getDriverPhone())
                .vehicleNumber(request.getVehicleNumber())
                .notes(request.getNotes())
                .createdByUserId(userId)
                .isCurrentRoute(true)
                .build();

        Route savedRoute = routeRepository.save(route);
        log.info("Route created with ID: {}", savedRoute.getId());

        return RouteResponse.fromEntity(savedRoute);
    }

    // ==================== READ ====================

    @Override
    public RouteResponse getRouteById(Long id) {
        log.info("Getting route by id: {}", id);
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found with id: " + id
                ));
        return RouteResponse.fromEntity(route);
    }

    @Override
    public RouteResponse getRouteByShipmentId(Long shipmentId) {
        log.info("Getting route for shipment: {}", shipmentId);
        Route route = routeRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found for shipment: " + shipmentId
                ));
        return RouteResponse.fromEntity(route);
    }

    /**
     * ✅ FIXED: Get current route for a shipment
     */
    @Override
    public RouteResponse getCurrentRoute(Long shipmentId) {
        log.info("Getting current route for shipment: {}", shipmentId);

        // Try to find current route
        java.util.Optional<Route> currentRouteOpt = routeRepository.findByShipmentIdAndIsCurrentRoute(shipmentId);

        if (currentRouteOpt.isPresent()) {
            return RouteResponse.fromEntity(currentRouteOpt.get());
        }

        // If no current route, get the latest route and set it as current
        List<Route> routes = routeRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId);
        if (!routes.isEmpty()) {
            Route latest = routes.get(0);
            latest.setIsCurrentRoute(true);
            Route saved = routeRepository.save(latest);
            log.info("Set latest route as current for shipment: {}", shipmentId);
            return RouteResponse.fromEntity(saved);
        }

        // No route found at all
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No route found for shipment: " + shipmentId
        );
    }

    /**
     * ✅ FIXED: Get route history for a shipment
     */
    @Override
    public List<RouteResponse> getRouteHistory(Long shipmentId) {
        log.info("Getting route history for shipment: {}", shipmentId);

        List<Route> routes = routeRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId);

        if (routes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No routes found for shipment: " + shipmentId
            );
        }

        return routes.stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteResponse> getAllRoutes() {
        log.info("Getting all routes");
        return routeRepository.findAll()
                .stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteResponse> getActiveRoutes() {
        log.info("Getting active routes");
        return routeRepository.findActiveRoutes()
                .stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteResponse> getRoutesByDriver(Long driverId) {
        log.info("Getting routes for driver: {}", driverId);
        return routeRepository.findByAssignedDriverId(driverId)
                .stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteResponse> getDelayedRoutes() {
        log.info("Getting delayed routes");
        return routeRepository.findDelayedRoutes()
                .stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE ====================

    @Override
    @Transactional
    public RouteResponse updateRoute(Long id, RouteUpdateRequest request, Long userId) {
        log.info("Updating route: {} by user: {}", id, userId);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found with id: " + id
                ));

        if (request.getAssignedDriverId() != null) {
            route.setAssignedDriverId(request.getAssignedDriverId());
        }
        if (request.getDriverName() != null) {
            route.setDriverName(request.getDriverName());
        }
        if (request.getDriverPhone() != null) {
            route.setDriverPhone(request.getDriverPhone());
        }
        if (request.getVehicleNumber() != null) {
            route.setVehicleNumber(request.getVehicleNumber());
        }
        if (request.getStatus() != null) {
            route.setStatus(request.getStatus());
            if ("DELIVERED".equals(request.getStatus())) {
                route.setActualDeliveryTime(LocalDateTime.now());
            }
        }
        if (request.getNotes() != null) {
            route.setNotes(request.getNotes());
        }

        route.setUpdatedByUserId(userId);

        Route updatedRoute = routeRepository.save(route);
        return RouteResponse.fromEntity(updatedRoute);
    }

    @Override
    @Transactional
    public RouteResponse updateDriver(Long id, Long driverId, String driverName, Long userId) {
        log.info("Updating driver for route: {} to {} by user: {}", id, driverId, userId);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found with id: " + id
                ));

        route.setAssignedDriverId(driverId);
        route.setDriverName(driverName);
        route.setUpdatedByUserId(userId);

        Route updatedRoute = routeRepository.save(route);
        return RouteResponse.fromEntity(updatedRoute);
    }

    @Override
    @Transactional
    public RouteResponse updateStatus(Long id, String status, Long userId) {
        log.info("Updating status for route: {} to {} by user: {}", id, status, userId);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found with id: " + id
                ));

        route.setStatus(status);
        if ("DELIVERED".equals(status)) {
            route.setActualDeliveryTime(LocalDateTime.now());
        }
        route.setUpdatedByUserId(userId);

        Route updatedRoute = routeRepository.save(route);
        return RouteResponse.fromEntity(updatedRoute);
    }

    // ==================== DELETE ====================

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        log.info("Deleting route: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found with id: " + id
                ));

        routeRepository.delete(route);
        log.info("Route deleted: {}", id);
    }
}
