package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.ShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.dto.ShipmentStatusUpdateRequest;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface ShipmentService {

    // Create
    ShipmentResponse createShipment(ShipmentRequest request, Long userId);

    // Read
    ShipmentResponse getShipmentById(Long id);
    ShipmentResponse getShipmentByTrackingNumber(String trackingNumber);
    List<ShipmentResponse> getAllShipments();
    List<ShipmentResponse> getShipmentsByUser(Long userId);
    Page<ShipmentResponse> getShipmentsByUserPaginated(Long userId, int page, int size);
    List<ShipmentResponse> getActiveShipmentsByUser(Long userId);
    List<ShipmentResponse> getActiveShipments();
    List<ShipmentResponse> searchShipments(String searchTerm);
    List<ShipmentResponse> getDelayedShipments();

    // Update
    ShipmentResponse updateShipment(Long shipmentId, ShipmentRequest request, Long userId);
    ShipmentResponse updateShipmentStatus(Long shipmentId, ShipmentStatusUpdateRequest request, Long userId);
    ShipmentResponse assignOperator(Long shipmentId, Long operatorId, Long userId);
    ShipmentResponse updateEstimatedDelivery(Long shipmentId, LocalDateTime newDate, Long userId);

    // Delete/Cancel
    void cancelShipment(Long shipmentId, String reason, Long userId);
    void deleteShipment(Long shipmentId);

    // Statistics
    long countShipmentsByStatus(Shipment.ShipmentStatus status);
    long countShipmentsByUserAndStatus(Long userId, Shipment.ShipmentStatus status);
}