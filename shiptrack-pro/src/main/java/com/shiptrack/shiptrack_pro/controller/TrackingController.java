package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.TrackingEventRequest;
import com.shiptrack.shiptrack_pro.dto.TrackingEventResponse;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.TrackingEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentRepository shipmentRepository;

    /**
     * POST /api/tracking/{shipmentId}/events - Add tracking event (Operator or Admin)
     */
    @PostMapping("/{shipmentId}/events")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<TrackingEventResponse> addTrackingEvent(
            @PathVariable Long shipmentId,
            @Valid @RequestBody TrackingEventRequest request,
            Authentication authentication) {

        log.info("Adding tracking event for shipment: {}", shipmentId);

        if (!shipmentRepository.existsById(shipmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found with id: " + shipmentId
            );
        }

        Long userId = getUserIdFromAuthentication(authentication);

        TrackingEvent event = TrackingEvent.builder()
                .shipmentId(shipmentId)
                .eventType(TrackingEvent.EventType.valueOf(request.getEventType().toUpperCase()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationName(request.getLocation())
                .description(request.getDescription())
                .status(TrackingEvent.EventStatus.COMPLETED)
                .recordedByUserId(userId)
                .eventTimestamp(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        TrackingEvent savedEvent = trackingEventRepository.save(event);
        log.info("Tracking event added with ID: {}", savedEvent.getId());

        return new ResponseEntity<>(TrackingEventResponse.fromEntity(savedEvent), HttpStatus.CREATED);
    }

    /**
     * GET /api/tracking/{shipmentId}/history - Get tracking history
     */
    @GetMapping("/{shipmentId}/history")
    public ResponseEntity<List<TrackingEventResponse>> getTrackingHistory(
            @PathVariable Long shipmentId) {

        log.info("Getting tracking history for shipment: {}", shipmentId);

        if (!shipmentRepository.existsById(shipmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found with id: " + shipmentId
            );
        }

        List<TrackingEventResponse> events = trackingEventRepository
                .findTrackingHistoryByShipment(shipmentId)
                .stream()
                .map(TrackingEventResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/tracking/{shipmentId}/latest - Get latest tracking event
     */
    @GetMapping("/{shipmentId}/latest")
    public ResponseEntity<TrackingEventResponse> getLatestTrackingEvent(
            @PathVariable Long shipmentId) {

        log.info("Getting latest tracking event for shipment: {}", shipmentId);

        List<TrackingEvent> events = trackingEventRepository
                .findByShipmentIdOrderByEventTimestampDesc(shipmentId);

        if (events.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No tracking events found for shipment: " + shipmentId
            );
        }

        return ResponseEntity.ok(TrackingEventResponse.fromEntity(events.get(0)));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return 1L;
        }
        return 1L; // In production, extract from JWT
    }
}