package com.shiptrack.shiptrack_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResponse {

    private String result;
    private Double latitude;
    private Double longitude;
    private String formattedAddress;
    private Double distanceKm;
    private Integer durationMinutes;
}