package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventResponse {

    private Long id;
    private Long shipmentId;
    private String eventType;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String description;
    private String status;
    private Long recordedByUserId;
    private LocalDateTime eventTimestamp;
    private String notes;
    private LocalDateTime createdAt;

    public static TrackingEventResponse fromEntity(TrackingEvent event) {
        if (event == null) return null;

        return TrackingEventResponse.builder()
                .id(event.getId())
                .shipmentId(event.getShipmentId())
                .eventType(event.getEventType() != null ? event.getEventType().name() : null)
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .locationName(event.getLocationName())
                .description(event.getDescription())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .recordedByUserId(event.getRecordedByUserId())
                .eventTimestamp(event.getEventTimestamp())
                .notes(event.getNotes())
                .createdAt(event.getCreatedAt())
                .build();
    }
}