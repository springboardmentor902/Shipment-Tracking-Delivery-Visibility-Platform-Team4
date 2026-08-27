package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.service.MapService;
import com.shiptrack.shiptrack_pro.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final MapService mapService;

    @Override
    public Route createRoute(Long shipmentId, Long driverId, String originAddress, String destinationAddress) {

        BigDecimal distanceKm = null;
        Integer estimatedTimeMinutes = null;

        try {
            double[] originCoords = mapService.geocodeAddress(originAddress);
            double[] destinationCoords = mapService.geocodeAddress(destinationAddress);

            Map<String, Object> distanceResult = mapService.getDistanceAndDuration(
                    originCoords[0], originCoords[1],
                    destinationCoords[0], destinationCoords[1]
            );

            distanceKm = (BigDecimal) distanceResult.get("distanceKm");
            estimatedTimeMinutes = (Integer) distanceResult.get("estimatedTimeMinutes");

        } catch (Exception e) {
            // Maps API failed (geocoding or distance lookup) — log it, but don't block route creation
            System.err.println("Map service failed for route creation: " + e.getMessage());
        }

        Route route = Route.builder()
                .shipmentId(shipmentId)
                .driverId(driverId)
                .origin(originAddress)
                .destination(destinationAddress)
                .distanceKm(distanceKm)
                .estimatedTimeMinutes(estimatedTimeMinutes)
                .build();

        return routeRepository.save(route);
    }

    @Override
    public Route getRouteByShipmentId(Long shipmentId) {
        return routeRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new RuntimeException("No route found for shipment id: " + shipmentId));
    }

    @Override
    public Route assignDriver(Long shipmentId, Long driverId) {
        Route route = routeRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new RuntimeException("No route found for shipment id: " + shipmentId));

        route.setDriverId(driverId);
        return routeRepository.save(route);
    }
}