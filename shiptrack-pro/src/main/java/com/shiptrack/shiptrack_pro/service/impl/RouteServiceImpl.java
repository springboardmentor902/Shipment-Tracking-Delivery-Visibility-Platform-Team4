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

        // Step 1: convert both addresses into coordinates
        double[] originCoords = mapService.geocodeAddress(originAddress);
        double[] destinationCoords = mapService.geocodeAddress(destinationAddress);

        // Step 2: get real driving distance and time between them
        Map<String, Object> distanceResult = mapService.getDistanceAndDuration(
                originCoords[0], originCoords[1],
                destinationCoords[0], destinationCoords[1]
        );

        // Step 3: build the Route with real calculated values
        Route route = Route.builder()
                .shipmentId(shipmentId)
                .driverId(driverId)
                .origin(originAddress)
                .destination(destinationAddress)
                .distanceKm((BigDecimal) distanceResult.get("distanceKm"))
                .estimatedTimeMinutes((Integer) distanceResult.get("estimatedTimeMinutes"))
                .build();

        // Step 4: save it to the routes table
        return routeRepository.save(route);
    }
}