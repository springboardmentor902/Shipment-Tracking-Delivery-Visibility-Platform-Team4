package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/shipments/{shipmentId}")
    public ResponseEntity<RouteResponse> createRoute(
            @PathVariable Long shipmentId,
            @Valid @RequestBody RouteRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        RouteResponse response =
                routeService.createRoute(
                        shipmentId,
                        request,
                        userEmail
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/shipments/{shipmentId}")
    public ResponseEntity<List<RouteResponse>> getRoutesByShipment(
            @PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                routeService.getRoutesByShipment(shipmentId)
        );
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getRouteById(
            @PathVariable Long routeId) {

        return ResponseEntity.ok(
                routeService.getRouteById(routeId)
        );
    }
}