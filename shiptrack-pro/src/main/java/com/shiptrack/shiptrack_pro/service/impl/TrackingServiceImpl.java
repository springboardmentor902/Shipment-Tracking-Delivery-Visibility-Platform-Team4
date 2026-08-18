package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.TrackingEventRequest;
import com.shiptrack.shiptrack_pro.dto.TrackingEventResponse;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.TrackingEventRepository;
import com.shiptrack.shiptrack_pro.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentRepository shipmentRepository;


    // =========================================================
    // CREATE TRACKING EVENT
    // =========================================================

    @Override
    public TrackingEventResponse createTrackingEvent(
            Long shipmentId,
            TrackingEventRequest request,
            String userEmail) {

        Shipment shipment =
                shipmentRepository.findById(shipmentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Shipment not found with id: "
                                                + shipmentId
                                )
                        );


        ShipmentStatus currentStatus =
                shipment.getStatus();

        ShipmentStatus newStatus =
                request.getStatus();


        // -----------------------------------------------------
        // Terminal-state protection
        // -----------------------------------------------------

        if (currentStatus == ShipmentStatus.DELIVERED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Delivered shipment cannot receive new tracking events"
            );
        }

        if (currentStatus == ShipmentStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cancelled shipment cannot receive new tracking events"
            );
        }


        // -----------------------------------------------------
        // Validate sequential status transition
        // -----------------------------------------------------

        if (!isValidStatusTransition(
                currentStatus,
                newStatus
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }


        // -----------------------------------------------------
        // Create tracking event
        // -----------------------------------------------------

        TrackingEvent event =
                TrackingEvent.builder()
                        .shipment(shipment)
                        .status(newStatus)
                        .locationText(
                                request.getLocationText()
                        )
                        .notes(
                                request.getNotes()
                        )
                        .updatedBy(
                                userEmail
                        )
                        .latitude(
                                request.getLatitude()
                        )
                        .longitude(
                                request.getLongitude()
                        )
                        .eventTimestamp(
                                LocalDateTime.now()
                        )
                        .build();


        TrackingEvent savedEvent =
                trackingEventRepository.save(event);


        // -----------------------------------------------------
        // Synchronize shipment status
        // -----------------------------------------------------

        shipment.setStatus(newStatus);


        // -----------------------------------------------------
        // Synchronize location
        // -----------------------------------------------------

        if (request.getLocationText() != null &&
                !request.getLocationText().isBlank()) {

            shipment.setCurrentLocation(
                    request.getLocationText()
            );
        }


        // -----------------------------------------------------
        // Delivered → destination
        // -----------------------------------------------------

        if (newStatus ==
                ShipmentStatus.DELIVERED) {

            shipment.setCurrentLocation(
                    shipment.getDestination()
            );
        }


        shipmentRepository.save(shipment);


        return mapToResponse(savedEvent);
    }


    // =========================================================
    // GET TRACKING EVENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEventResponse> getTrackingEvents(
            Long shipmentId) {

        if (!shipmentRepository.existsById(shipmentId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found with id: "
                            + shipmentId
            );
        }


        return trackingEventRepository
                .findByShipmentIdOrderByEventTimestampDesc(
                        shipmentId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // STATUS TRANSITION VALIDATION
    // =========================================================

    private boolean isValidStatusTransition(
            ShipmentStatus current,
            ShipmentStatus next) {

        if (current == null ||
                next == null) {

            return false;
        }


        switch (current) {

            case CREATED:

                return next ==
                        ShipmentStatus.PICKED_UP

                        || next ==
                        ShipmentStatus.CANCELLED;


            case PICKED_UP:

                return next ==
                        ShipmentStatus.IN_TRANSIT

                        || next ==
                        ShipmentStatus.CANCELLED;


            case IN_TRANSIT:

                return next ==
                        ShipmentStatus.OUT_FOR_DELIVERY

                        || next ==
                        ShipmentStatus.FAILED_DELIVERY

                        || next ==
                        ShipmentStatus.CANCELLED;


            case OUT_FOR_DELIVERY:

                return next ==
                        ShipmentStatus.DELIVERED

                        || next ==
                        ShipmentStatus.FAILED_DELIVERY

                        || next ==
                        ShipmentStatus.CANCELLED;


            case FAILED_DELIVERY:

                return next ==
                        ShipmentStatus.OUT_FOR_DELIVERY

                        || next ==
                        ShipmentStatus.CANCELLED;


            case DELIVERED:
            case CANCELLED:

                return false;


            default:

                return false;
        }
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private TrackingEventResponse mapToResponse(
            TrackingEvent event) {

        return TrackingEventResponse.builder()

                .id(event.getId())

                .shipmentId(
                        event.getShipment().getId()
                )

                .trackingNumber(
                        event.getShipment()
                                .getTrackingNumber()
                )

                .status(
                        event.getStatus()
                )

                .locationText(
                        event.getLocationText()
                )

                .notes(
                        event.getNotes()
                )

                .updatedBy(
                        event.getUpdatedBy()
                )

                .latitude(
                        event.getLatitude()
                )

                .longitude(
                        event.getLongitude()
                )

                .eventTimestamp(
                        event.getEventTimestamp()
                )

                .build();
    }
}