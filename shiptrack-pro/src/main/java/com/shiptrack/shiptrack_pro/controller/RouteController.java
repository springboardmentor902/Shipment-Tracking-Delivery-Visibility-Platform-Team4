package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.CreateRouteRequest;
import com.shiptrack.shiptrack_pro.dto.AssignDriverRequest;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<Route> createRoute(@RequestBody CreateRouteRequest request) {
        Route route = routeService.createRoute(
                request.getShipmentId(),
                request.getDriverId(),
                request.getOriginAddress(),
                request.getDestinationAddress()
        );
        return ResponseEntity.ok(route);
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<Route> getRouteByShipmentId(@PathVariable Long shipmentId) {
        Route route = routeService.getRouteByShipmentId(shipmentId);
        return ResponseEntity.ok(route);
    }

    @PatchMapping("/{shipmentId}/driver")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<Route> assignDriver(
            @PathVariable Long shipmentId,
            @RequestBody AssignDriverRequest request
    ) {
        Route route = routeService.assignDriver(shipmentId, request.getDriverId());
        return ResponseEntity.ok(route);
    }
}