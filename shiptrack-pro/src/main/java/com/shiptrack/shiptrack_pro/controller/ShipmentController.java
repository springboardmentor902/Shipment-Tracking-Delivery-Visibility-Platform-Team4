package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.ShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.dto.ShipmentStatusUpdateRequest;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody ShipmentRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Creating shipment for user: {}", userId);
        ShipmentResponse response = shipmentService.createShipment(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
        log.info("Getting all shipments");
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
        log.info("Getting shipment by id: {}", id);
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(
            @PathVariable String trackingNumber) {
        log.info("Getting shipment by tracking number: {}", trackingNumber);
        return ResponseEntity.ok(shipmentService.getShipmentByTrackingNumber(trackingNumber));
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShipmentResponse>> getMyShipments(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Getting shipments for user: {}", userId);
        return ResponseEntity.ok(shipmentService.getShipmentsByUser(userId));
    }

    @GetMapping("/user/paginated")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_CLIENT', 'ADMINISTRATOR')")
    public ResponseEntity<Page<ShipmentResponse>> getMyShipmentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Getting paginated shipments for user: {}", userId);
        return ResponseEntity.ok(shipmentService.getShipmentsByUserPaginated(userId, page, size));
    }

    @GetMapping("/user/active")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_CLIENT', 'ADMINISTRATOR')")
    public ResponseEntity<List<ShipmentResponse>> getMyActiveShipments(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Getting active shipments for user: {}", userId);
        return ResponseEntity.ok(shipmentService.getActiveShipmentsByUser(userId));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<ShipmentResponse>> getActiveShipments() {
        log.info("Getting active shipments");
        return ResponseEntity.ok(shipmentService.getActiveShipments());
    }

    @GetMapping("/delayed")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<List<ShipmentResponse>> getDelayedShipments() {
        log.info("Getting delayed shipments");
        return ResponseEntity.ok(shipmentService.getDelayedShipments());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ShipmentResponse>> searchShipments(
            @RequestParam String term) {
        log.info("Searching shipments with term: {}", term);
        return ResponseEntity.ok(shipmentService.searchShipments(term));
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<ShipmentResponse> updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating shipment: {} by user: {}", id, userId);
        return ResponseEntity.ok(shipmentService.updateShipment(id, request, userId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentStatusUpdateRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating status for shipment: {} to {} by user: {}", id, request.getStatus(), userId);
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, request, userId));
    }

    @PatchMapping("/{id}/assign-operator/{operatorId}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<ShipmentResponse> assignOperator(
            @PathVariable Long id,
            @PathVariable Long operatorId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Assigning operator {} to shipment: {} by user: {}", operatorId, id, userId);
        return ResponseEntity.ok(shipmentService.assignOperator(id, operatorId, userId));
    }

    @PatchMapping("/{id}/estimated-delivery")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<ShipmentResponse> updateEstimatedDelivery(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating estimated delivery for shipment: {} by user: {}", id, userId);
        return ResponseEntity.ok(shipmentService.updateEstimatedDelivery(id, newDate, userId));
    }

    // ==================== DELETE/CANCEL ====================

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<Void> cancelShipment(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Cancelling shipment: {} by user: {}", id, userId);
        shipmentService.cancelShipment(id, reason, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        log.info("Deleting shipment: {}", id);
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== STATISTICS ====================

    @GetMapping("/stats/status/{status}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<Long> countByStatus(@PathVariable String status) {
        try {
            Shipment.ShipmentStatus shipmentStatus = Shipment.ShipmentStatus.valueOf(status.toUpperCase());
            long count = shipmentService.countShipmentsByStatus(shipmentStatus);
            log.info("Count for status {}: {}", status, count);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== HELPER ====================

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 1L;
        }
        // In production, extract from JWT
        return 1L;
    }
}