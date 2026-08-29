package com.shiptrack.shiptrack_pro.dto;

import lombok.Data;

@Data
public class GeocodingResponse {

    private String displayName;
    private double latitude;
    private double longitude;
}