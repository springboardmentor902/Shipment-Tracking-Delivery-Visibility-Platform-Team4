package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.dto.ShipmentStatusUpdateRequest;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.TrackingEventRepository;
import com.shiptrack.shiptrack_pro.service.PackageService;
import com.shiptrack.shiptrack_pro.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final PackageService packageService;  // ✅ Added PackageService

    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request, Long userId) {
        log.info("Creating shipment for user: {}", userId);

        // Generate unique tracking number
        String trackingNumber = generateTrackingNumber();

        // Build shipment entity
        Shipment shipment = Shipment.builder()
                .trackingNumber(trackingNumber)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .senderName(request.getSenderName())
                .senderEmail(request.getSenderEmail())
                .senderPhone(request.getSenderPhone())
                .senderAddress(request.getSenderAddress())
                .recipientName(request.getRecipientName())
                .recipientEmail(request.getRecipientEmail())
                .recipientPhone(request.getRecipientPhone())
                .recipientAddress(request.getRecipientAddress())
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .itemDescription(request.getItemDescription())
                .itemValue(request.getItemValue())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .fragile(request.getFragile() != null ? request.getFragile() : false)
                .priority(request.getPriority() != null ? request.getPriority() : "STANDARD")
                .status(Shipment.ShipmentStatus.CREATED)
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .createdByUserId(userId)
                .businessId(request.getBusinessId())
                .assignedOperatorId(request.getAssignedOperatorId())
                .notes(request.getNotes())
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        log.info("Shipment created with ID: {}, Tracking: {}", savedShipment.getId(), savedShipment.getTrackingNumber());

        // ✅ CREATE TRACKING EVENT FOR CREATED STATUS
        createTrackingEvent(
                savedShipment.getId(),
                TrackingEvent.EventType.CREATED,
                "Shipment created with tracking number: " + trackingNumber,
                null,
                userId,
                "Shipment created"
        );

        // ✅ SAVE PACKAGES IF PROVIDED
        if (request.getPackages() != null && !request.getPackages().isEmpty()) {
            log.info("Creating {} packages for shipment: {}", request.getPackages().size(), savedShipment.getId());
            for (PackageRequest pkgRequest : request.getPackages()) {
                packageService.createPackage(savedShipment.getId(), pkgRequest);
            }
        }

        return ShipmentResponse.fromEntity(savedShipment);
    }

    @Override
    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + id
                ));
        return ShipmentResponse.fromEntity(shipment);
    }

    @Override
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with tracking number: " + trackingNumber
                ));
        return ShipmentResponse.fromEntity(shipment);
    }

    @Override
    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll()
                .stream()
                .map(ShipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> getShipmentsByUser(Long userId) {
        return shipmentRepository.findByCreatedByUserId(userId)
                .stream()
                .map(ShipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ShipmentResponse> getShipmentsByUserPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return shipmentRepository.findByCreatedByUserId(userId, pageable)
                .map(ShipmentResponse::fromEntity);
    }

    @Override
    public List<ShipmentResponse> getActiveShipmentsByUser(Long userId) {
        return shipmentRepository.findActiveShipmentsByUser(userId)
                .stream()
                .map(ShipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> searchShipments(String searchTerm) {
        return shipmentRepository.searchShipments(searchTerm)
                .stream()
                .map(ShipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentResponse> getDelayedShipments() {
        return shipmentRepository.findDelayedShipments()
                .stream()
                .map(ShipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipment(Long shipmentId, ShipmentRequest request, Long userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId
                ));

        if (shipment.isDelivered()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update a delivered shipment"
            );
        }

        if (shipment.isCancelled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update a cancelled shipment"
            );
        }

        // Update fields
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());
        shipment.setSenderName(request.getSenderName());
        shipment.setSenderEmail(request.getSenderEmail());
        shipment.setSenderPhone(request.getSenderPhone());
        shipment.setSenderAddress(request.getSenderAddress());
        shipment.setRecipientName(request.getRecipientName());
        shipment.setRecipientEmail(request.getRecipientEmail());
        shipment.setRecipientPhone(request.getRecipientPhone());
        shipment.setRecipientAddress(request.getRecipientAddress());
        shipment.setPickupAddress(request.getPickupAddress());
        shipment.setDeliveryAddress(request.getDeliveryAddress());
        shipment.setWeight(request.getWeight());
        shipment.setDimensions(request.getDimensions());
        shipment.setItemDescription(request.getItemDescription());
        shipment.setItemValue(request.getItemValue());
        shipment.setQuantity(request.getQuantity());
        shipment.setFragile(request.getFragile());
        shipment.setPriority(request.getPriority());
        shipment.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        shipment.setBusinessId(request.getBusinessId());
        shipment.setAssignedOperatorId(request.getAssignedOperatorId());
        shipment.setNotes(request.getNotes());
        shipment.setLastUpdatedByUserId(userId);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        // ✅ UPDATE PACKAGES IF PROVIDED
        if (request.getPackages() != null && !request.getPackages().isEmpty()) {
            // Delete existing packages
            packageService.deletePackagesByShipment(shipmentId);
            // Create new packages
            for (PackageRequest pkgRequest : request.getPackages()) {
                packageService.createPackage(shipmentId, pkgRequest);
            }
            log.info("Updated {} packages for shipment: {}", request.getPackages().size(), shipmentId);
        }

        // ✅ CREATE TRACKING EVENT FOR UPDATE
        createTrackingEvent(
                updatedShipment.getId(),
                TrackingEvent.EventType.LOCATION_UPDATE,
                "Shipment details updated",
                null,
                userId,
                "Shipment updated"
        );

        return ShipmentResponse.fromEntity(updatedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipmentStatus(Long shipmentId, ShipmentStatusUpdateRequest request, Long userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId
                ));

        validateStatusTransition(shipment.getStatus(), request.getStatus());

        Shipment.ShipmentStatus oldStatus = shipment.getStatus();
        shipment.setStatus(request.getStatus());
        shipment.setLastUpdatedByUserId(userId);

        // Set delivery date if DELIVERED
        if (request.getStatus() == Shipment.ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
        }

        // Set cancellation info if CANCELLED
        if (request.getStatus() == Shipment.ShipmentStatus.CANCELLED) {
            shipment.setCancelledAt(LocalDateTime.now());
            if (request.getNotes() != null) {
                shipment.setCancellationReason(request.getNotes());
            }
        }

        // Update location
        if (request.getLocation() != null) {
            shipment.setCurrentLocation(request.getLocation());
        }

        // Update notes
        if (request.getNotes() != null) {
            shipment.setNotes(request.getNotes());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);

        // ✅ CREATE TRACKING EVENT FOR STATUS CHANGE
        createTrackingEvent(
                updatedShipment.getId(),
                TrackingEvent.EventType.valueOf(request.getStatus().name()),
                "Status changed from " + oldStatus + " to " + request.getStatus(),
                request.getLocation(),
                userId,
                request.getNotes()
        );

        log.info("Tracking event created for status change: {} -> {}", oldStatus, request.getStatus());

        return ShipmentResponse.fromEntity(updatedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponse assignOperator(Long shipmentId, Long operatorId, Long userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId
                ));

        if (shipment.isDelivered() || shipment.isCancelled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign operator to delivered or cancelled shipment"
            );
        }

        shipment.setAssignedOperatorId(operatorId);
        shipment.setLastUpdatedByUserId(userId);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        // ✅ CREATE TRACKING EVENT FOR OPERATOR ASSIGNMENT
        createTrackingEvent(
                updatedShipment.getId(),
                TrackingEvent.EventType.LOCATION_UPDATE,
                "Operator assigned to shipment",
                null,
                userId,
                "Assigned operator ID: " + operatorId
        );

        return ShipmentResponse.fromEntity(updatedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateEstimatedDelivery(Long shipmentId, LocalDateTime newDate, Long userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId
                ));

        if (shipment.isDelivered() || shipment.isCancelled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update delivery date for delivered or cancelled shipment"
            );
        }

        shipment.setExpectedDeliveryDate(newDate);
        shipment.setLastUpdatedByUserId(userId);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        // ✅ CREATE TRACKING EVENT FOR DELIVERY DATE UPDATE
        createTrackingEvent(
                updatedShipment.getId(),
                TrackingEvent.EventType.LOCATION_UPDATE,
                "Estimated delivery date updated",
                null,
                userId,
                "New delivery date: " + newDate
        );

        return ShipmentResponse.fromEntity(updatedShipment);
    }

    @Override
    @Transactional
    public void cancelShipment(Long shipmentId, String reason, Long userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId
                ));

        if (shipment.isDelivered()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot cancel a delivered shipment"
            );
        }

        Shipment.ShipmentStatus oldStatus = shipment.getStatus();
        shipment.setStatus(Shipment.ShipmentStatus.CANCELLED);
        shipment.setCancelledAt(LocalDateTime.now());
        shipment.setCancellationReason(reason);
        shipment.setLastUpdatedByUserId(userId);

        shipmentRepository.save(shipment);

        // ✅ CREATE TRACKING EVENT FOR CANCELLATION
        createTrackingEvent(
                shipment.getId(),
                TrackingEvent.EventType.CANCELLED,
                "Shipment cancelled",
                null,
                userId,
                "Reason: " + reason
        );
    }

    @Override
    @Transactional
    public void deleteShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId
                ));

        // Only allow deletion of CREATED shipments
        if (shipment.getStatus() != Shipment.ShipmentStatus.CREATED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only shipments with status CREATED can be deleted"
            );
        }

        // ✅ Delete associated packages first
        packageService.deletePackagesByShipment(shipmentId);

        shipmentRepository.delete(shipment);
    }

    @Override
    public long countShipmentsByStatus(Shipment.ShipmentStatus status) {
        return shipmentRepository.findByStatus(status).size();
    }

    @Override
    public long countShipmentsByUserAndStatus(Long userId, Shipment.ShipmentStatus status) {
        return shipmentRepository.findByCreatedByUserId(userId)
                .stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }

    @Override
    public List<ShipmentResponse> getActiveShipments() {
        return shipmentRepository.findActiveShipments()
                .stream()
                .map(ShipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private void createTrackingEvent(Long shipmentId, TrackingEvent.EventType eventType,
                                     String description, String location,
                                     Long userId, String notes) {
        try {
            TrackingEvent trackingEvent = TrackingEvent.builder()
                    .shipmentId(shipmentId)
                    .eventType(eventType)
                    .description(description)
                    .locationName(location)
                    .status(TrackingEvent.EventStatus.COMPLETED)
                    .recordedByUserId(userId)
                    .eventTimestamp(LocalDateTime.now())
                    .notes(notes)
                    .build();

            trackingEventRepository.save(trackingEvent);
            log.info("Tracking event created: {} for shipment: {}", eventType, shipmentId);
        } catch (Exception e) {
            log.error("Failed to create tracking event: {}", e.getMessage());
            // Don't throw exception - tracking event failure shouldn't stop shipment operations
        }
    }

    private String generateTrackingNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "SHIP-" + timestamp + "-" + random;
    }

    private void validateStatusTransition(Shipment.ShipmentStatus currentStatus,
                                          Shipment.ShipmentStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {
            case CREATED:
                if (newStatus != Shipment.ShipmentStatus.PICKED_UP &&
                        newStatus != Shipment.ShipmentStatus.CANCELLED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "From CREATED, can only transition to PICKED_UP or CANCELLED"
                    );
                }
                break;
            case PICKED_UP:
                if (newStatus != Shipment.ShipmentStatus.IN_TRANSIT &&
                        newStatus != Shipment.ShipmentStatus.CANCELLED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "From PICKED_UP, can only transition to IN_TRANSIT or CANCELLED"
                    );
                }
                break;
            case IN_TRANSIT:
                if (newStatus != Shipment.ShipmentStatus.OUT_FOR_DELIVERY &&
                        newStatus != Shipment.ShipmentStatus.FAILED_DELIVERY &&
                        newStatus != Shipment.ShipmentStatus.CANCELLED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "From IN_TRANSIT, can only transition to OUT_FOR_DELIVERY, FAILED_DELIVERY, or CANCELLED"
                    );
                }
                break;
            case OUT_FOR_DELIVERY:
                if (newStatus != Shipment.ShipmentStatus.DELIVERED &&
                        newStatus != Shipment.ShipmentStatus.FAILED_DELIVERY &&
                        newStatus != Shipment.ShipmentStatus.CANCELLED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "From OUT_FOR_DELIVERY, can only transition to DELIVERED, FAILED_DELIVERY, or CANCELLED"
                    );
                }
                break;
            case FAILED_DELIVERY:
                if (newStatus != Shipment.ShipmentStatus.OUT_FOR_DELIVERY &&
                        newStatus != Shipment.ShipmentStatus.CANCELLED &&
                        newStatus != Shipment.ShipmentStatus.RETURNED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "From FAILED_DELIVERY, can only transition to OUT_FOR_DELIVERY, CANCELLED, or RETURNED"
                    );
                }
                break;
            case DELIVERED:
            case CANCELLED:
            case RETURNED:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot change status from terminal state: " + currentStatus
                );
            default:
                break;
        }
    }
}