package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.dto.RouteUpdateRequest;
import com.shiptrack.shiptrack_pro.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    // ==================== CREATE ====================

    /**
     * POST /api/routes - Create route (Operator/Admin only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<RouteResponse> createRoute(
            @Valid @RequestBody RouteRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Creating route for shipment: {} by user: {}", request.getShipmentId(), userId);
        RouteResponse response = routeService.createRoute(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    /**
     * GET /api/routes - Get all routes (Operator/Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        log.info("Getting all routes");
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    /**
     * GET /api/routes/active - Get active routes
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<RouteResponse>> getActiveRoutes() {
        log.info("Getting active routes");
        return ResponseEntity.ok(routeService.getActiveRoutes());
    }

    /**
     * GET /api/routes/delayed - Get delayed routes
     */
    @GetMapping("/delayed")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<RouteResponse>> getDelayedRoutes() {
        log.info("Getting delayed routes");
        return ResponseEntity.ok(routeService.getDelayedRoutes());
    }

    /**
     * GET /api/routes/{id} - Get route by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        log.info("Getting route by id: {}", id);
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    /**
     * GET /api/routes/shipment/{shipmentId} - Get route for a shipment
     */
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<RouteResponse> getRouteByShipmentId(@PathVariable Long shipmentId) {
        log.info("Getting route for shipment: {}", shipmentId);
        return ResponseEntity.ok(routeService.getRouteByShipmentId(shipmentId));
    }

    /**
     * ✅ NEW: GET /api/routes/{shipmentId}/history - Get route history
     */
    @GetMapping("/{shipmentId}/history")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<RouteResponse>> getRouteHistory(@PathVariable Long shipmentId) {
        log.info("Getting route history for shipment: {}", shipmentId);
        return ResponseEntity.ok(routeService.getRouteHistory(shipmentId));
    }

    /**
     * ✅ NEW: GET /api/routes/{shipmentId}/current - Get current route
     */
    @GetMapping("/{shipmentId}/current")
    public ResponseEntity<RouteResponse> getCurrentRoute(@PathVariable Long shipmentId) {
        log.info("Getting current route for shipment: {}", shipmentId);
        return ResponseEntity.ok(routeService.getCurrentRoute(shipmentId));
    }

    /**
     * GET /api/routes/driver/{driverId} - Get routes by driver
     */
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<RouteResponse>> getRoutesByDriver(@PathVariable Long driverId) {
        log.info("Getting routes for driver: {}", driverId);
        return ResponseEntity.ok(routeService.getRoutesByDriver(driverId));
    }

    // ==================== UPDATE ====================

    /**
     * PUT /api/routes/{id} - Update route (Operator/Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<RouteResponse> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating route: {} by user: {}", id, userId);
        return ResponseEntity.ok(routeService.updateRoute(id, request, userId));
    }

    /**
     * PATCH /api/routes/{id}/driver - Update driver (Operator/Admin only)
     */
    @PatchMapping("/{id}/driver")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<RouteResponse> updateDriver(
            @PathVariable Long id,
            @RequestParam Long driverId,
            @RequestParam String driverName,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating driver for route: {} to {} by user: {}", id, driverId, userId);
        return ResponseEntity.ok(routeService.updateDriver(id, driverId, driverName, userId));
    }

    /**
     * PATCH /api/routes/{id}/status - Update status (Operator/Admin only)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<RouteResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating status for route: {} to {} by user: {}", id, status, userId);
        return ResponseEntity.ok(routeService.updateStatus(id, status, userId));
    }

    // ==================== DELETE ====================

    /**
     * DELETE /api/routes/{id} - Delete route (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        log.info("Deleting route: {}", id);
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== HELPER ====================

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return 1L;
        }
        return 1L;
    }
}