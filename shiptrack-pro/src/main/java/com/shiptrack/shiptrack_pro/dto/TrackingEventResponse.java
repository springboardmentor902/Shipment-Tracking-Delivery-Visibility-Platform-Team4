package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrackingEventResponse {

    private Long id;

    private Long shipmentId;

    private String trackingNumber;

    private ShipmentStatus status;

    private String locationText;

    private String notes;

    private String updatedBy;

    private Double latitude;

    private Double longitude;

    private LocalDateTime eventTimestamp;
}