package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.GeocodingResponse;
import com.shiptrack.shiptrack_pro.service.OSMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
public class GeocodingController {

    private final OSMService osmService;

    @GetMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GeocodingResponse> geocodeAddress(@RequestParam String address) {
        log.info("Geocoding address: {}", address);

        if (address == null || address.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    GeocodingResponse.builder()
                            .result("Address is required")
                            .build()
            );
        }

        try {
            OSMService.GeocodeResult result = osmService.geocodeAddress(address.trim());

            if (result == null) {
                log.warn("Geocoding failed for: {}", address);
                return ResponseEntity.badRequest().body(
                        GeocodingResponse.builder()
                                .result("Failed to geocode address: " + address)
                                .build()
                );
            }

            return ResponseEntity.ok(
                    GeocodingResponse.builder()
                            .result("Success")
                            .latitude(result.getLat())
                            .longitude(result.getLng())
                            .formattedAddress(result.getFormattedAddress())
                            .build()
            );
        } catch (Exception e) {
            log.error("Geocoding error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    GeocodingResponse.builder()
                            .result("Error: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/distance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GeocodingResponse> calculateDistance(
            @RequestParam String origin,
            @RequestParam String destination) {

        log.info("Calculating distance from {} to {}", origin, destination);

        if (origin == null || origin.trim().isEmpty() ||
                destination == null || destination.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    GeocodingResponse.builder()
                            .result("Origin and destination are required")
                            .build()
            );
        }

        try {
            // ✅ Try multiple geocoding attempts with different address formats
            OSMService.GeocodeResult originGeocode = geocodeWithFallback(origin.trim());
            OSMService.GeocodeResult destGeocode = geocodeWithFallback(destination.trim());

            if (originGeocode == null) {
                log.warn("Failed to geocode origin: {}", origin);
                return ResponseEntity.badRequest().body(
                        GeocodingResponse.builder()
                                .result("Failed to geocode origin: " + origin + ". Please try a more specific address.")
                                .build()
                );
            }

            if (destGeocode == null) {
                log.warn("Failed to geocode destination: {}", destination);
                return ResponseEntity.badRequest().body(
                        GeocodingResponse.builder()
                                .result("Failed to geocode destination: " + destination + ". Please try a more specific address.")
                                .build()
                );
            }

            // Calculate distance
            OSMService.DistanceResult distance = osmService.calculateDistance(
                    originGeocode.getLat(), originGeocode.getLng(),
                    destGeocode.getLat(), destGeocode.getLng()
            );

            if (distance == null) {
                log.warn("Failed to calculate distance from {} to {}", origin, destination);
                return ResponseEntity.badRequest().body(
                        GeocodingResponse.builder()
                                .result("Failed to calculate distance. Please try again.")
                                .build()
                );
            }

            return ResponseEntity.ok(
                    GeocodingResponse.builder()
                            .result("Success")
                            .distanceKm(distance.getDistanceKm())
                            .durationMinutes(distance.getDurationMinutes())
                            .formattedAddress(originGeocode.getFormattedAddress() + " → " + destGeocode.getFormattedAddress())
                            .build()
            );
        } catch (Exception e) {
            log.error("Distance calculation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    GeocodingResponse.builder()
                            .result("Error: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/reverse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GeocodingResponse> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {

        log.info("Reverse geocoding: {}, {}", lat, lng);

        try {
            OSMService.GeocodeResult result = osmService.reverseGeocode(lat, lng);

            if (result == null) {
                log.warn("Failed to reverse geocode: {}, {}", lat, lng);
                return ResponseEntity.badRequest().body(
                        GeocodingResponse.builder()
                                .result("Failed to reverse geocode")
                                .build()
                );
            }

            return ResponseEntity.ok(
                    GeocodingResponse.builder()
                            .result("Success")
                            .latitude(lat)
                            .longitude(lng)
                            .formattedAddress(result.getFormattedAddress())
                            .build()
            );
        } catch (Exception e) {
            log.error("Reverse geocoding error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    GeocodingResponse.builder()
                            .result("Error: " + e.getMessage())
                            .build()
            );
        }
    }

    // ✅ Helper method with fallback
    private OSMService.GeocodeResult geocodeWithFallback(String address) {
        // First attempt with original address
        OSMService.GeocodeResult result = osmService.geocodeAddress(address);
        if (result != null) {
            return result;
        }

        // Try with "India" suffix if not already present
        if (!address.toLowerCase().contains("india")) {
            String withCountry = address + ", India";
            log.info("Retrying geocoding with country: {}", withCountry);
            result = osmService.geocodeAddress(withCountry);
            if (result != null) {
                return result;
            }
        }

        // Try with just city name (remove extra text)
        String[] parts = address.split(",");
        if (parts.length > 0) {
            String cityOnly = parts[0].trim();
            log.info("Retrying geocoding with city only: {}", cityOnly);
            result = osmService.geocodeAddress(cityOnly + ", India");
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}