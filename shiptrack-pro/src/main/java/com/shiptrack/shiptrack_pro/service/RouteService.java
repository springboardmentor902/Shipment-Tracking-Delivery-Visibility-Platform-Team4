package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;

import java.util.List;

public interface RouteService {

    RouteResponse createRoute(
            Long shipmentId,
            RouteRequest request,
            String userEmail
    );

    List<RouteResponse> getRoutesByShipment(
            Long shipmentId
    );

    RouteResponse getRouteById(Long routeId);
}