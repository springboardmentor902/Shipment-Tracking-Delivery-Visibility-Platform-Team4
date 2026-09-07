package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventRequest {

    @NotBlank(message = "Event type is required")
    private String eventType;

    private Double latitude;
    private Double longitude;
    private String location;

    @NotBlank(message = "Description is required")
    private String description;

    private String notes;
}