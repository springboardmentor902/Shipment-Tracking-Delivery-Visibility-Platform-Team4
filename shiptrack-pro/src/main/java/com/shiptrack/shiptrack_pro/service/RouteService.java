package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.entity.Route;

public interface RouteService {
    Route createRoute(Long shipmentId, Long driverId, String originAddress, String destinationAddress);
}