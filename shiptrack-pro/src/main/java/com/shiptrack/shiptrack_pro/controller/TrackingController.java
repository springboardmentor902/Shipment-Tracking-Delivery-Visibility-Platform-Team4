package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.TrackingEventRequest;
import com.shiptrack.shiptrack_pro.dto.TrackingEventResponse;
import com.shiptrack.shiptrack_pro.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/shipments/{shipmentId}/events")
    public ResponseEntity<TrackingEventResponse> createTrackingEvent(
            @PathVariable Long shipmentId,
            @Valid @RequestBody TrackingEventRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TrackingEventResponse response =
                trackingService.createTrackingEvent(
                        shipmentId,
                        request,
                        userEmail
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/shipments/{shipmentId}/events")
    public ResponseEntity<List<TrackingEventResponse>> getTrackingEvents(
            @PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                trackingService.getTrackingEvents(shipmentId)
        );
    }
}