
package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.RouteAlternativeDTO;
import com.shiptrack.shiptrack_pro.service.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes/optimization")
@RequiredArgsConstructor
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    /**
     * Get all available route alternatives.
     */
    @GetMapping
    public ResponseEntity<List<RouteAlternativeDTO>> getOptimizedRoutes(
            @RequestParam String origin,
            @RequestParam String destination) {

        return ResponseEntity.ok(
                routeOptimizationService.getOptimizedRoutes(
                        origin,
                        destination
                )
        );
    }

    /**
     * Get the best route based on traffic-adjusted duration.
     */
    @GetMapping("/best")
    public ResponseEntity<RouteAlternativeDTO> getBestRoute(
            @RequestParam String origin,
            @RequestParam String destination) {

        RouteAlternativeDTO route =
                routeOptimizationService.getBestRoute(
                        origin,
                        destination
                );

        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(route);
    }

    /**
     * Get the shortest-distance route.
     */
    @GetMapping("/shortest")
    public ResponseEntity<RouteAlternativeDTO> getShortestRoute(
            @RequestParam String origin,
            @RequestParam String destination) {

        RouteAlternativeDTO route =
                routeOptimizationService.getShortestRoute(
                        origin,
                        destination
                );

        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(route);
    }

    /**
     * Get the fastest route without traffic adjustment.
     */
    @GetMapping("/fastest")
    public ResponseEntity<RouteAlternativeDTO> getFastestRoute(
            @RequestParam String origin,
            @RequestParam String destination) {

        RouteAlternativeDTO route =
                routeOptimizationService.getFastestRoute(
                        origin,
                        destination
                );

        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(route);
    }

    /**
     * Get a route by type.
     * Example types: Driving, Cycling, Walking, Balanced.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<RouteAlternativeDTO> getRouteByType(
            @PathVariable String type,
            @RequestParam String origin,
            @RequestParam String destination) {

        RouteAlternativeDTO route =
                routeOptimizationService.getRouteByType(
                        origin,
                        destination,
                        type
                );

        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(route);
    }
}