
package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.ShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.ShipmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;


    // =========================================================
    // CREATE SHIPMENT
    // =========================================================

    @Override
    public ShipmentResponse createShipment(
            ShipmentRequest request,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        // Only customer/business client should create shipments.
        if (!isRole(user, "CUSTOMER") &&
                !isRole(user, "BUSINESS_CLIENT")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to create shipments"
            );
        }

        Shipment shipment = new Shipment();

        shipment.setTrackingNumber(
                generateTrackingNumber()
        );

        shipment.setSender(request.getSender());
        shipment.setReceiver(request.getReceiver());
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());

        shipment.setCurrentLocation(
                request.getCurrentLocation() != null &&
                        !request.getCurrentLocation().isBlank()
                        ? request.getCurrentLocation()
                        : request.getOrigin()
        );

        shipment.setEstimatedDelivery(
                request.getEstimatedDelivery()
        );

        shipment.setStatus(
                ShipmentStatus.CREATED
        );

        shipment.setUser(user);

        Shipment savedShipment =
                shipmentRepository.save(shipment);

        return convertToResponse(savedShipment);
    }


    // =========================================================
    // GET SHIPMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments(
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        /*
         * Business clients/customers:
         * return only their own shipments.
         *
         * Logistics operators/admins:
         * return all shipments for operational visibility.
         */

        if (isRole(user, "ADMINISTRATOR") ||
                isRole(user, "LOGISTICS_OPERATOR")) {

            return shipmentRepository.findAll()
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }

        return shipmentRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET SHIPMENT BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(
            Long id,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        Shipment shipment;

        if (isRole(user, "ADMINISTRATOR") ||
                isRole(user, "LOGISTICS_OPERATOR")) {

            shipment = shipmentRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Shipment not found"
                            )
                    );

        } else {

            shipment = shipmentRepository
                    .findByIdAndUserId(
                            id,
                            user.getId()
                    )
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Shipment not found"
                            )
                    );
        }

        return convertToResponse(shipment);
    }


    // =========================================================
    // UPDATE SHIPMENT DETAILS
    // =========================================================

    @Override
    public ShipmentResponse updateShipment(
            Long id,
            ShipmentRequest request,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        Shipment shipment =
                findShipmentForModification(
                        id,
                        user
                );

        if (shipment.getStatus() ==
                ShipmentStatus.DELIVERED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Delivered shipment cannot be updated"
            );
        }

        if (shipment.getStatus() ==
                ShipmentStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cancelled shipment cannot be updated"
            );
        }

        shipment.setSender(request.getSender());
        shipment.setReceiver(request.getReceiver());
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());

        if (request.getCurrentLocation() != null &&
                !request.getCurrentLocation().isBlank()) {

            shipment.setCurrentLocation(
                    request.getCurrentLocation()
            );
        }

        shipment.setEstimatedDelivery(
                request.getEstimatedDelivery()
        );

        Shipment updatedShipment =
                shipmentRepository.save(shipment);

        return convertToResponse(updatedShipment);
    }


    // =========================================================
    // UPDATE SHIPMENT STATUS
    // =========================================================

    @Override
    public ShipmentResponse updateShipmentStatus(
            Long id,
            ShipmentStatus newStatus,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        /*
         * Only logistics operators and administrators
         * should move shipment workflow status.
         */

        if (!isRole(user, "LOGISTICS_OPERATOR") &&
                !isRole(user, "ADMINISTRATOR")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update shipment status"
            );
        }

        Shipment shipment =
                shipmentRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Shipment not found"
                                )
                        );

        ShipmentStatus currentStatus =
                shipment.getStatus();

        if (currentStatus ==
                ShipmentStatus.DELIVERED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Delivered shipment cannot change status"
            );
        }

        if (currentStatus ==
                ShipmentStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cancelled shipment cannot change status"
            );
        }

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

        shipment.setStatus(newStatus);

        if (newStatus ==
                ShipmentStatus.DELIVERED) {

            shipment.setCurrentLocation(
                    shipment.getDestination()
            );
        }

        Shipment updatedShipment =
                shipmentRepository.save(shipment);

        return convertToResponse(updatedShipment);
    }


    // =========================================================
    // CANCEL SHIPMENT
    // =========================================================

    @Override
    public ShipmentResponse cancelShipment(
            Long id,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        Shipment shipment =
                findShipmentForModification(
                        id,
                        user
                );

        if (shipment.getStatus() ==
                ShipmentStatus.DELIVERED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Delivered shipment cannot be cancelled"
            );
        }

        if (shipment.getStatus() ==
                ShipmentStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Shipment is already cancelled"
            );
        }

        shipment.setStatus(
                ShipmentStatus.CANCELLED
        );

        Shipment cancelledShipment =
                shipmentRepository.save(shipment);

        return convertToResponse(
                cancelledShipment
        );
    }


    // =========================================================
    // FIND USER
    // =========================================================

    private User getUserByEmail(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
    }


    // =========================================================
    // FIND SHIPMENT FOR MODIFICATION
    // =========================================================

    private Shipment findShipmentForModification(
            Long id,
            User user
    ) {

        /*
         * Admins and logistics operators can
         * operate on any shipment.
         */

        if (isRole(user, "ADMINISTRATOR") ||
                isRole(user, "LOGISTICS_OPERATOR")) {

            return shipmentRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Shipment not found"
                            )
                    );
        }

        /*
         * Customers/business clients can only
         * modify their own shipment.
         */

        return shipmentRepository
                .findByIdAndUserId(
                        id,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Shipment not found"
                        )
                );
    }


    // =========================================================
    // ROLE CHECK
    // =========================================================

    private boolean isRole(
            User user,
            String role
    ) {

        return role.equalsIgnoreCase(
                user.getRole()
        );
    }


    // =========================================================
    // STATUS TRANSITION VALIDATION
    // =========================================================

    private boolean isValidStatusTransition(
            ShipmentStatus current,
            ShipmentStatus next
    ) {

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
    // GENERATE TRACKING NUMBER
    // =========================================================

    private String generateTrackingNumber() {

        return "ST-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private ShipmentResponse convertToResponse(
            Shipment shipment
    ) {

        return new ShipmentResponse(

                shipment.getId(),

                shipment.getTrackingNumber(),

                shipment.getSender(),

                shipment.getReceiver(),

                shipment.getOrigin(),

                shipment.getDestination(),

                shipment.getCurrentLocation(),

                shipment.getStatus(),

                shipment.getEstimatedDelivery(),

                shipment.getCreatedAt(),

                shipment.getUpdatedAt()
        );
    }
}

