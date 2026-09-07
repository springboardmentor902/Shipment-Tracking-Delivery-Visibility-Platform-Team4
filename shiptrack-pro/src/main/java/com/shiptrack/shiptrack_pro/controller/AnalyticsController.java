package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/routes
     * Get route analytics (Admin only)
     */
    @GetMapping("/routes")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> getRouteAnalytics() {
        log.info("Fetching route analytics");
        try {
            return ResponseEntity.ok(analyticsService.getRouteAnalytics());
        } catch (Exception e) {
            log.error("Error fetching route analytics: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch analytics");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * ✅ FIXED: GET /api/analytics/routes/alternatives
     * Get route alternatives for a specific origin-destination pair
     */
    @GetMapping("/routes/alternatives")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> getRouteAlternatives(
            @RequestParam String origin,
            @RequestParam String destination) {

        log.info("Fetching route alternatives from '{}' to '{}'", origin, destination);

        try {
            // Validate inputs
            if (origin == null || origin.trim().isEmpty() ||
                    destination == null || destination.trim().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Origin and destination are required"
                );
            }

            Map<String, Object> result = analyticsService.getRouteAlternatives(origin, destination);
            log.info("Found {} route alternatives", result.getOrDefault("count", 0));

            return ResponseEntity.ok(result);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching route alternatives: {}", e.getMessage(), e);

            // Return empty result instead of failing
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("alternatives", new java.util.ArrayList<>());
            errorResponse.put("count", 0);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to fetch route alternatives. Please try again.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}