package com.shiptrack.shiptrack_pro.dto;

import lombok.Data;

@Data
public class CreateRouteRequest {
    private Long shipmentId;
    private Long driverId;
    private String originAddress;
    private String destinationAddress;
}