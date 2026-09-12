package com.shiptrack.shiptrack_pro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrack.shiptrack_pro.dto.RouteAlternativeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OSMService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ✅ OSM URLs
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String REVERSE_NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse";
    private static final String OSRM_URL = "https://router.project-osrm.org/route/v1/driving";

    // ✅ OSRM Profiles for alternatives
    private static final String OSRM_CAR_URL = "https://router.project-osrm.org/route/v1/driving";
    private static final String OSRM_BIKE_URL = "https://router.project-osrm.org/route/v1/cycling";
    private static final String OSRM_WALK_URL = "https://router.project-osrm.org/route/v1/walking";

    // ✅ Known coordinates for common Indian cities (fallback)
    private static final java.util.Map<String, double[]> CITY_COORDINATES = new java.util.HashMap<>();
    static {
        CITY_COORDINATES.put("mumbai", new double[]{19.0760, 72.8777});
        CITY_COORDINATES.put("delhi", new double[]{28.7041, 77.1025});
        CITY_COORDINATES.put("bangalore", new double[]{12.9716, 77.5946});
        CITY_COORDINATES.put("chennai", new double[]{13.0827, 80.2707});
        CITY_COORDINATES.put("hyderabad", new double[]{17.3850, 78.4867});
        CITY_COORDINATES.put("kolkata", new double[]{22.5726, 88.3639});
        CITY_COORDINATES.put("pune", new double[]{18.5204, 73.8567});
        CITY_COORDINATES.put("ahmedabad", new double[]{23.0225, 72.5714});
        CITY_COORDINATES.put("surat", new double[]{21.1702, 72.8311});
        CITY_COORDINATES.put("jaipur", new double[]{26.9124, 75.7873});
        CITY_COORDINATES.put("lucknow", new double[]{26.8467, 80.9462});
        CITY_COORDINATES.put("kanpur", new double[]{26.4499, 80.3319});
        CITY_COORDINATES.put("nagpur", new double[]{21.1458, 79.0882});
        CITY_COORDINATES.put("indore", new double[]{22.7196, 75.8577});
        CITY_COORDINATES.put("bhopal", new double[]{23.2599, 77.4126});
        CITY_COORDINATES.put("visakhapatnam", new double[]{17.6868, 83.2185});
        CITY_COORDINATES.put("vijayawada", new double[]{16.5062, 80.6480});
        CITY_COORDINATES.put("coimbatore", new double[]{11.0168, 76.9558});
        CITY_COORDINATES.put("kochi", new double[]{9.9312, 76.2673});
        CITY_COORDINATES.put("thiruvananthapuram", new double[]{8.5241, 76.9366});
    }

    public OSMService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ==================== GEOCODING METHODS ====================

    /**
     * Geocode address to coordinates using OpenStreetMap Nominatim with fallback
     */
    public GeocodeResult geocodeAddress(String address) {
        try {
            String cleanAddress = address.trim().toLowerCase();

            // Try to get from cache first
            double[] cachedCoords = getCachedCoordinates(cleanAddress);
            if (cachedCoords != null) {
                log.info("Using cached coordinates for: {}", address);
                return new GeocodeResult(cachedCoords[0], cachedCoords[1], address);
            }

            // Try OSM geocoding
            URI uri = UriComponentsBuilder.fromUriString(NOMINATIM_URL)
                    .queryParam("q", address.trim())
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ShipTrackPro/1.0 (contact@shiptrack.com)");
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("OSM Geocoding: {}", address);
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonArray = objectMapper.readTree(response.getBody());

                if (jsonArray.isArray() && jsonArray.size() > 0) {
                    JsonNode first = jsonArray.get(0);
                    if (first.has("lat") && first.has("lon")) {
                        double lat = first.get("lat").asDouble();
                        double lon = first.get("lon").asDouble();
                        String displayName = first.has("display_name") ?
                                first.get("display_name").asText() : address;

                        log.info("OSM Result: {} -> ({}, {})", displayName, lat, lon);
                        return new GeocodeResult(lat, lon, displayName);
                    }
                }
                log.warn("OSM No results for: {}", address);
            } else {
                log.warn("OSM returned status: {}", response.getStatusCode());
            }

            // If OSM fails, try to extract city name and use cached coordinates
            String cityName = extractCityName(address);
            if (cityName != null) {
                double[] coords = getCachedCoordinates(cityName);
                if (coords != null) {
                    log.info("Using fallback coordinates for city: {}", cityName);
                    return new GeocodeResult(coords[0], coords[1], cityName);
                }
            }

            return null;

        } catch (RestClientException e) {
            log.error("OSM Geocoding connection error: {}", e.getMessage());
            String cityName = extractCityName(address);
            if (cityName != null) {
                double[] coords = getCachedCoordinates(cityName);
                if (coords != null) {
                    log.info("Using fallback coordinates after connection error: {}", cityName);
                    return new GeocodeResult(coords[0], coords[1], cityName);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("OSM Geocoding error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Reverse geocode coordinates to address
     */
    public GeocodeResult reverseGeocode(double lat, double lon) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(REVERSE_NOMINATIM_URL)
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("format", "json")
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ShipTrackPro/1.0 (contact@shiptrack.com)");
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("OSM Reverse Geocoding: ({}, {})", lat, lon);
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                if (jsonNode.has("display_name")) {
                    String displayName = jsonNode.get("display_name").asText();
                    log.info("OSM Reverse Result: {}", displayName);
                    return new GeocodeResult(lat, lon, displayName);
                }
            }
            return new GeocodeResult(lat, lon, "Lat: " + lat + ", Lng: " + lon);

        } catch (Exception e) {
            log.error("OSM Reverse Geocoding error: {}", e.getMessage());
            return new GeocodeResult(lat, lon, "Lat: " + lat + ", Lng: " + lon);
        }
    }

    // ==================== DISTANCE CALCULATION METHODS ====================

    /**
     * Calculate distance and duration using OpenStreetMap OSRM
     */
    public DistanceResult calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        try {
            String coordinates = lon1 + "," + lat1 + ";" + lon2 + "," + lat2;

            URI uri = UriComponentsBuilder.fromUriString(OSRM_URL + "/" + coordinates)
                    .queryParam("overview", "false")
                    .build()
                    .toUri();

            log.info("OSRM Calculating distance from ({}, {}) to ({}, {})", lat1, lon1, lat2, lon2);
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                if (jsonNode.has("code") && "Ok".equals(jsonNode.get("code").asText())) {
                    JsonNode routes = jsonNode.get("routes");
                    if (routes.isArray() && routes.size() > 0) {
                        JsonNode route = routes.get(0);
                        double distance = route.get("distance").asDouble() / 1000.0;
                        int duration = route.get("duration").asInt() / 60;

                        log.info("OSRM Distance: {} km, Duration: {} min", distance, duration);
                        return new DistanceResult(distance, duration);
                    }
                }
            }

            // Fallback: Calculate straight-line distance using Haversine formula
            double distance = calculateHaversineDistance(lat1, lon1, lat2, lon2);
            int duration = (int) (distance / 50 * 60); // Assume 50 km/h average speed

            log.info("Using fallback distance calculation: {} km, {} min", distance, duration);
            return new DistanceResult(distance, duration);

        } catch (Exception e) {
            log.error("OSRM Error: {}", e.getMessage());
            double distance = calculateHaversineDistance(lat1, lon1, lat2, lon2);
            int duration = (int) (distance / 50 * 60);
            return new DistanceResult(distance, duration);
        }
    }

    // ==================== ✅ NEW: ROUTE ALTERNATIVES METHODS ====================

    /**
     * Get route alternatives using OSRM with different profiles
     */
    public List<RouteAlternativeDTO> getRouteAlternatives(String originAddress, String destinationAddress) {
        log.info("Getting route alternatives from {} to {}", originAddress, destinationAddress);

        List<RouteAlternativeDTO> alternatives = new ArrayList<>();

        // First, geocode both addresses
        GeocodeResult originGeocode = geocodeAddress(originAddress);
        GeocodeResult destGeocode = geocodeAddress(destinationAddress);

        if (originGeocode == null || destGeocode == null) {
            log.warn("Failed to geocode addresses for route alternatives");
            return alternatives;
        }

        double originLat = originGeocode.getLat();
        double originLng = originGeocode.getLng();
        double destLat = destGeocode.getLat();
        double destLng = destGeocode.getLng();

        // Try different OSRM profiles
        String[] profiles = {"driving", "cycling", "walking"};
        String[] profileNames = {"Driving", "Cycling", "Walking"};

        for (int i = 0; i < profiles.length; i++) {
            try {
                String coordinates = originLng + "," + originLat + ";" + destLng + "," + destLat;

                String url = "https://router.project-osrm.org/route/v1/" + profiles[i] + "/" + coordinates;

                URI uri = UriComponentsBuilder.fromUriString(url)
                        .queryParam("overview", "full")
                        .queryParam("geometries", "polyline")
                        .queryParam("steps", "false")
                        .build()
                        .toUri();

                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "ShipTrackPro/1.0 (contact@shiptrack.com)");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                log.info("OSRM Alternative ({}) for: {} to {}", profiles[i], originAddress, destinationAddress);
                ResponseEntity<String> response = restTemplate.exchange(
                        uri, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());

                    if (jsonNode.has("code") && "Ok".equals(jsonNode.get("code").asText())) {
                        JsonNode routes = jsonNode.get("routes");
                        if (routes.isArray() && routes.size() > 0) {
                            JsonNode route = routes.get(0);
                            JsonNode legs = route.get("legs").get(0);

                            double distanceKm = legs.get("distance").asDouble() / 1000.0;
                            int durationMinutes = (int) (legs.get("duration").asDouble() / 60.0);
                            String geometry = route.has("geometry") ? route.get("geometry").asText() : null;

                            // Add some traffic variation (simulated)
                            int trafficDuration = durationMinutes + (int)(Math.random() * 10);

                            RouteAlternativeDTO alternative = RouteAlternativeDTO.builder()
                                    .distanceKm(distanceKm)
                                    .durationMinutes(durationMinutes)
                                    .trafficDurationMinutes(trafficDuration)
                                    .summary(profileNames[i] + " Route")
                                    .polyline(geometry)
                                    .originAddress(originAddress)
                                    .destinationAddress(destinationAddress)
                                    .routeType(profileNames[i])
                                    .build();

                            alternatives.add(alternative);
                            log.info("Added {} alternative: {} km, {} min (traffic: {} min)",
                                    profileNames[i], distanceKm, durationMinutes, trafficDuration);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to get {} route alternative: {}", profiles[i], e.getMessage());
            }
        }

        // If we have at least two alternatives, add a "Balanced" option
        if (alternatives.size() >= 2) {
            RouteAlternativeDTO first = alternatives.get(0);
            RouteAlternativeDTO second = alternatives.get(1);

            double avgDistance = (first.getDistanceKm() + second.getDistanceKm()) / 2;
            int avgDuration = (first.getDurationMinutes() + second.getDurationMinutes()) / 2;

            RouteAlternativeDTO balanced = RouteAlternativeDTO.builder()
                    .distanceKm(avgDistance)
                    .durationMinutes(avgDuration)
                    .trafficDurationMinutes(avgDuration + 5)
                    .summary("Balanced Route")
                    .polyline(first.getPolyline() != null ? first.getPolyline() : null)
                    .originAddress(originAddress)
                    .destinationAddress(destinationAddress)
                    .routeType("Balanced")
                    .build();

            alternatives.add(balanced);
            log.info("Added Balanced alternative: {} km, {} min", avgDistance, avgDuration);
        }

        log.info("Total alternatives found: {}", alternatives.size());
        return alternatives;
    }

    /**
     * Get route with specific profile
     */
    public RouteAlternativeDTO getRouteWithProfile(String originAddress, String destinationAddress, String profile) {
        List<RouteAlternativeDTO> alternatives = getRouteAlternatives(originAddress, destinationAddress);

        return alternatives.stream()
                .filter(a -> a.getRouteType() != null && a.getRouteType().equalsIgnoreCase(profile))
                .findFirst()
                .orElse(null);
    }

    // ==================== HELPER METHODS ====================

    private double[] getCachedCoordinates(String address) {
        if (address == null) return null;
        String key = address.toLowerCase().trim();
        return CITY_COORDINATES.get(key);
    }

    private String extractCityName(String address) {
        if (address == null) return null;
        String[] parts = address.split(",");
        if (parts.length > 0) {
            return parts[0].trim().toLowerCase();
        }
        return address.trim().toLowerCase();
    }

    /**
     * Haversine formula to calculate distance between two points
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

         // ==================== INNER CLASSES ====================

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class GeocodeResult {
        private double lat;
        private double lng;
        private String formattedAddress;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DistanceResult {
        private double distanceKm;
        private int durationMinutes;
    }
}