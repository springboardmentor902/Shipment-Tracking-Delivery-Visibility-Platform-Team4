package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;
    private String trackingNumber;
    private String origin;
    private String destination;
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String senderAddress;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String recipientAddress;
    private String pickupAddress;
    private String deliveryAddress;
    private Double weight;
    private String dimensions;
    private String itemDescription;
    private Double itemValue;
    private Integer quantity;
    private Boolean fragile;
    private String priority;
    private String status;
    private String currentLocation;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Long createdByUserId;
    private Long businessId;
    private Long assignedOperatorId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ShipmentResponse fromEntity(Shipment shipment) {
        if (shipment == null) return null;

        return ShipmentResponse.builder()
                .id(shipment.getId())
                .trackingNumber(shipment.getTrackingNumber())
                .origin(shipment.getOrigin())
                .destination(shipment.getDestination())
                .senderName(shipment.getSenderName())
                .senderEmail(shipment.getSenderEmail())
                .senderPhone(shipment.getSenderPhone())
                .senderAddress(shipment.getSenderAddress())
                .recipientName(shipment.getRecipientName())
                .recipientEmail(shipment.getRecipientEmail())
                .recipientPhone(shipment.getRecipientPhone())
                .recipientAddress(shipment.getRecipientAddress())
                .pickupAddress(shipment.getPickupAddress())
                .deliveryAddress(shipment.getDeliveryAddress())
                .weight(shipment.getWeight())
                .dimensions(shipment.getDimensions())
                .itemDescription(shipment.getItemDescription())
                .itemValue(shipment.getItemValue())
                .quantity(shipment.getQuantity())
                .fragile(shipment.getFragile())
                .priority(shipment.getPriority())
                .status(shipment.getStatus() != null ? shipment.getStatus().name() : null)
                .currentLocation(shipment.getCurrentLocation())
                .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                .actualDeliveryDate(shipment.getActualDeliveryDate())
                .cancelledAt(shipment.getCancelledAt())
                .cancellationReason(shipment.getCancellationReason())
                .createdByUserId(shipment.getCreatedByUserId())
                .businessId(shipment.getBusinessId())
                .assignedOperatorId(shipment.getAssignedOperatorId())
                .notes(shipment.getNotes())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}