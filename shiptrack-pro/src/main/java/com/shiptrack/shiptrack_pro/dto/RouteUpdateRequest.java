package com.shiptrack.shiptrack_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteUpdateRequest {

    private Long assignedDriverId;
    private String driverName;
    private String driverPhone;
    private String vehicleNumber;
    private String status;
    private String notes;
}