package com.shiptrack.shiptrack_pro.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class MapService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Geocoding: address -> latitude/longitude, using Nominatim
    public double[] geocodeAddress(String address) {
        String url = "https://nominatim.openstreetmap.org/search?q="
                + address.replace(" ", "+")
                + "&format=json&limit=1";

        // Nominatim requires a User-Agent header, or it rejects the request
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "ShipTrackPro/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, List.class);

        List<Map<String, Object>> results = response.getBody();
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("No location found for address: " + address);
        }

        Map<String, Object> first = results.get(0);
        double lat = Double.parseDouble((String) first.get("lat"));
        double lon = Double.parseDouble((String) first.get("lon"));
        return new double[]{lat, lon};
    }

    // 2. Distance + duration between two points, using OSRM
    public Map<String, Object> getDistanceAndDuration(double lat1, double lon1, double lat2, double lon2) {
        String url = String.format(
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                lon1, lat1, lon2, lat2);

        Map response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
        if (routes == null || routes.isEmpty()) {
            throw new RuntimeException("No route found between the given points");
        }

        Map<String, Object> route = routes.get(0);
        double distanceMeters = ((Number) route.get("distance")).doubleValue();
        double durationSeconds = ((Number) route.get("duration")).doubleValue();

        BigDecimal distanceKm = BigDecimal.valueOf(distanceMeters / 1000.0);
        int durationMinutes = (int) Math.round(durationSeconds / 60.0);

        return Map.of(
                "distanceKm", distanceKm,
                "estimatedTimeMinutes", durationMinutes
        );
    }
}