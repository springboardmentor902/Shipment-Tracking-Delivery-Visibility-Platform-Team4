package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    public RouteResponse createRoute(
            Long shipmentId,
            RouteRequest request,
            String userEmail) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Shipment not found with id: " + shipmentId
                        )
                );

        Route route = Route.builder()
                .shipment(shipment)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .routeName(request.getRouteName())
                .distanceKm(request.getDistanceKm())
                .estimatedDurationMinutes(
                        request.getEstimatedDurationMinutes()
                )
                .assignedBy(userEmail)
                .build();

        Route savedRoute = routeRepository.save(route);

        return mapToResponse(savedRoute);
    }

    @Override
    public List<RouteResponse> getRoutesByShipment(
            Long shipmentId) {

        if (!shipmentRepository.existsById(shipmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found with id: " + shipmentId
            );
        }

        return routeRepository
                .findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RouteResponse getRouteById(Long routeId) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Route not found with id: " + routeId
                        )
                );

        return mapToResponse(route);
    }

    private RouteResponse mapToResponse(Route route) {

        return RouteResponse.builder()
                .id(route.getId())
                .shipmentId(route.getShipment().getId())
                .trackingNumber(
                        route.getShipment().getTrackingNumber()
                )
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .routeName(route.getRouteName())
                .distanceKm(route.getDistanceKm())
                .estimatedDurationMinutes(
                        route.getEstimatedDurationMinutes()
                )
                .assignedBy(route.getAssignedBy())
                .createdAt(route.getCreatedAt())
                .build();
    }
}