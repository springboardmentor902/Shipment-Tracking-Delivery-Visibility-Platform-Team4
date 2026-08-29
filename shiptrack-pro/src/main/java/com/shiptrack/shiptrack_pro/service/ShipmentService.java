package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.ShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;

import java.util.List;

public interface ShipmentService {

    ShipmentResponse createShipment(
            ShipmentRequest request,
            String userEmail
    );

    List<ShipmentResponse> getAllShipments(
            String userEmail
    );

    ShipmentResponse getShipmentById(
            Long id,
            String userEmail
    );

    ShipmentResponse updateShipment(
            Long id,
            ShipmentRequest request,
            String userEmail
    );

    ShipmentResponse updateShipmentStatus(
            Long id,
            ShipmentStatus status,
            String userEmail
    );

    ShipmentResponse cancelShipment(
            Long id,
            String userEmail
    );

    ShipmentResponse assignOperator(
            Long shipmentId,
            Long operatorId,
            String userEmail
    );
}