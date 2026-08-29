package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;

    private String trackingNumber;

    private String sender;

    private String receiver;

    private String origin;

    private String destination;

    private String currentLocation;

    private ShipmentStatus status;

    private LocalDateTime estimatedDelivery;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}