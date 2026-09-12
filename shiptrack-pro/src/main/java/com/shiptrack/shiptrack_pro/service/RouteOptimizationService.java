package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.RouteAlternativeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final OSMService osmService;

    /**
     * Get optimized route recommendations using OSM
     */
    public List<RouteAlternativeDTO> getOptimizedRoutes(String origin, String destination) {
        log.info("Getting optimized routes from {} to {}", origin, destination);
        return osmService.getRouteAlternatives(origin, destination);
    }

    /**
     * Find the best route based on traffic conditions (simulated)
     */
    public RouteAlternativeDTO getBestRoute(String origin, String destination) {
        List<RouteAlternativeDTO> alternatives = getOptimizedRoutes(origin, destination);

        if (alternatives.isEmpty()) {
            return null;
        }

        // Find route with shortest duration
        return alternatives.stream()
                .min((r1, r2) -> Integer.compare(
                        r1.getTrafficDurationMinutes(),
                        r2.getTrafficDurationMinutes()
                ))
                .orElse(null);
    }

    /**
     * Find the shortest distance route
     */
    public RouteAlternativeDTO getShortestRoute(String origin, String destination) {
        List<RouteAlternativeDTO> alternatives = getOptimizedRoutes(origin, destination);

        if (alternatives.isEmpty()) {
            return null;
        }

        return alternatives.stream()
                .min((r1, r2) -> Double.compare(
                        r1.getDistanceKm(),
                        r2.getDistanceKm()
                ))
                .orElse(null);
    }

    /**
     * Find the fastest route (without traffic)
     */
    public RouteAlternativeDTO getFastestRoute(String origin, String destination) {
        List<RouteAlternativeDTO> alternatives = getOptimizedRoutes(origin, destination);

        if (alternatives.isEmpty()) {
            return null;
        }

        return alternatives.stream()
                .min((r1, r2) -> Integer.compare(
                        r1.getDurationMinutes(),
                        r2.getDurationMinutes()
                ))
                .orElse(null);
    }

    /**
     * Get route by specific type
     */
    public RouteAlternativeDTO getRouteByType(String origin, String destination, String type) {
        List<RouteAlternativeDTO> alternatives = getOptimizedRoutes(origin, destination);

        if (alternatives.isEmpty()) {
            return null;
        }

        return alternatives.stream()
                .filter(a -> a.getRouteType() != null && a.getRouteType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }
}