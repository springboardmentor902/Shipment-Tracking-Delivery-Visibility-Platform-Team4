package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;  // ✅ ADD THIS

@Entity
@Table(name = "shipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "destination", nullable = false)
    private String destination;

    // Sender Details
    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "sender_phone")
    private String senderPhone;

    @Column(name = "sender_address")
    private String senderAddress;

    // Recipient Details
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "recipient_address")
    private String recipientAddress;

    // Pickup & Delivery
    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    // Package Details
    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "dimensions")
    private String dimensions; // L x W x H in cm

    @Column(name = "item_description", nullable = false)
    private String itemDescription;

    @Column(name = "item_value")
    private Double itemValue;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "priority")
    private String priority; // STANDARD, EXPRESS, PRIORITY

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;

    // Dates
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // Audit Fields
    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "assigned_operator_id")
    private Long assignedOperatorId;

    @Column(name = "last_updated_by_user_id")
    private Long lastUpdatedByUserId;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum for Shipment Status
    public enum ShipmentStatus {
        CREATED,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED_DELIVERY,
        CANCELLED,
        RETURNED
    }

    // Helper methods
    public boolean isActive() {
        return status != ShipmentStatus.CANCELLED &&
                status != ShipmentStatus.DELIVERED &&
                status != ShipmentStatus.RETURNED;
    }

    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return status == ShipmentStatus.CANCELLED;
    }
}