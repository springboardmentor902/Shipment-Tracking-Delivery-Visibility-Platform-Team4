package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.dto.RouteUpdateRequest;

import java.util.List;

public interface RouteService {

    // ===== CREATE =====
    RouteResponse createRoute(RouteRequest request, Long userId);

    // ===== READ =====
    RouteResponse getRouteById(Long id);
    RouteResponse getRouteByShipmentId(Long shipmentId);
    RouteResponse getCurrentRoute(Long shipmentId);
    List<RouteResponse> getRouteHistory(Long shipmentId);
    List<RouteResponse> getAllRoutes();
    List<RouteResponse> getActiveRoutes();
    List<RouteResponse> getRoutesByDriver(Long driverId);
    List<RouteResponse> getDelayedRoutes();

    // ===== UPDATE =====
    RouteResponse updateRoute(Long id, RouteUpdateRequest request, Long userId);
    RouteResponse updateDriver(Long id, Long driverId, String driverName, Long userId);
    RouteResponse updateStatus(Long id, String status, Long userId);

    // ===== DELETE =====
    void deleteRoute(Long id);
}