package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    private String senderEmail;
    private String senderPhone;
    private String senderAddress;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    private String recipientEmail;
    private String recipientPhone;
    private String recipientAddress;

    private String pickupAddress;
    private String deliveryAddress;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    private String dimensions;

    @NotBlank(message = "Item description is required")
    private String itemDescription;

    @Positive(message = "Item value must be positive")
    private Double itemValue;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private Boolean fragile;
    private String priority;

    private LocalDateTime expectedDeliveryDate;
    private Long businessId;
    private Long assignedOperatorId;
    private String notes;
    private List<PackageRequest> packages;
}