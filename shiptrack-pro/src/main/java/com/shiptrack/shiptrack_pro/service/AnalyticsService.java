package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.RouteAlternativeDTO;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RouteRepository routeRepository;
    private final RouteOptimizationService routeOptimizationService;

    /**
     * Get route analytics
     */
    public Map<String, Object> getRouteAnalytics() {
        log.info("Getting route analytics");

        Map<String, Object> analytics = new HashMap<>();

        try {
            // Get all routes
            List<Route> allRoutes = routeRepository.findAll();
            List<Route> activeRoutes = routeRepository.findActiveRoutes();
            List<Route> delayedRoutes = routeRepository.findDelayedRoutes();
            List<Route> currentRoutes = routeRepository.findAllCurrentRoutes();

            // Basic counts
            analytics.put("totalRoutes", allRoutes.size());
            analytics.put("activeRoutes", activeRoutes.size());
            analytics.put("delayedRoutes", delayedRoutes.size());
            analytics.put("currentRoutes", currentRoutes.size());

            // Average distance
            double avgDistance = allRoutes.stream()
                    .filter(r -> r.getDistanceKm() != null)
                    .mapToDouble(Route::getDistanceKm)
                    .average()
                    .orElse(0);
            analytics.put("averageDistanceKm", Math.round(avgDistance * 100.0) / 100.0);

            // Average duration
            double avgDuration = allRoutes.stream()
                    .filter(r -> r.getEstimatedDurationMinutes() != null)
                    .mapToInt(Route::getEstimatedDurationMinutes)
                    .average()
                    .orElse(0);
            analytics.put("averageDurationMinutes", Math.round(avgDuration));

            // Total distance
            double totalDistance = allRoutes.stream()
                    .filter(r -> r.getDistanceKm() != null)
                    .mapToDouble(Route::getDistanceKm)
                    .sum();
            analytics.put("totalDistanceKm", Math.round(totalDistance * 100.0) / 100.0);

            // Completion rate
            long deliveredCount = allRoutes.stream()
                    .filter(r -> "DELIVERED".equals(r.getStatus()))
                    .count();
            double completionRate = allRoutes.isEmpty() ? 0 :
                    (double) deliveredCount / allRoutes.size() * 100;
            analytics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

            // Status distribution
            Map<String, Long> statusDistribution = new HashMap<>();
            for (Route route : allRoutes) {
                String status = route.getStatus() != null ? route.getStatus() : "UNKNOWN";
                statusDistribution.put(status, statusDistribution.getOrDefault(status, 0L) + 1);
            }
            analytics.put("statusDistribution", statusDistribution);

            // Routes by driver
            Map<Long, Long> routesByDriver = new HashMap<>();
            for (Route route : allRoutes) {
                if (route.getAssignedDriverId() != null) {
                    routesByDriver.put(route.getAssignedDriverId(),
                            routesByDriver.getOrDefault(route.getAssignedDriverId(), 0L) + 1);
                }
            }
            analytics.put("routesByDriver", routesByDriver);

            log.info("Route analytics complete: {}", analytics);

        } catch (Exception e) {
            log.error("Error getting route analytics: {}", e.getMessage());
            analytics.put("error", e.getMessage());
        }

        return analytics;
    }

    /**
     * Get route alternative analytics with better error handling
     */
    public Map<String, Object> getRouteAlternatives(String origin, String destination) {
        log.info("Getting route alternatives from {} to {}", origin, destination);

        Map<String, Object> result = new HashMap<>();

        try {
            // ✅ Use RouteOptimizationService to get alternatives
            List<RouteAlternativeDTO> alternatives = routeOptimizationService.getOptimizedRoutes(origin, destination);

            result.put("alternatives", alternatives != null ? alternatives : new ArrayList<>());
            result.put("count", alternatives != null ? alternatives.size() : 0);

            if (alternatives != null && !alternatives.isEmpty()) {
                // Find best options
                RouteAlternativeDTO bestTraffic = alternatives.stream()
                        .min((r1, r2) -> Integer.compare(
                                r1.getTrafficDurationMinutes() != null ? r1.getTrafficDurationMinutes() : Integer.MAX_VALUE,
                                r2.getTrafficDurationMinutes() != null ? r2.getTrafficDurationMinutes() : Integer.MAX_VALUE
                        ))
                        .orElse(null);

                RouteAlternativeDTO shortest = alternatives.stream()
                        .min((r1, r2) -> Double.compare(
                                r1.getDistanceKm() != null ? r1.getDistanceKm() : Double.MAX_VALUE,
                                r2.getDistanceKm() != null ? r2.getDistanceKm() : Double.MAX_VALUE
                        ))
                        .orElse(null);

                RouteAlternativeDTO fastest = alternatives.stream()
                        .min((r1, r2) -> Integer.compare(
                                r1.getDurationMinutes() != null ? r1.getDurationMinutes() : Integer.MAX_VALUE,
                                r2.getDurationMinutes() != null ? r2.getDurationMinutes() : Integer.MAX_VALUE
                        ))
                        .orElse(null);

                result.put("bestTrafficTime", bestTraffic);
                result.put("shortestDistance", shortest);
                result.put("fastestTime", fastest);
            }

            log.info("Found {} route alternatives", result.get("count"));

        } catch (Exception e) {
            log.error("Error getting route alternatives: {}", e.getMessage(), e);
            result.put("alternatives", new ArrayList<>());
            result.put("count", 0);
            result.put("error", e.getMessage());
        }

        return result;
    }
}