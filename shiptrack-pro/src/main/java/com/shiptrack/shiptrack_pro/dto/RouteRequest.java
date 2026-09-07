package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotNull(message = "Shipment ID is required")
    private Long shipmentId;

    @NotBlank(message = "Origin address is required")
    private String originAddress;

    @NotBlank(message = "Destination address is required")
    private String destinationAddress;

    private Long assignedDriverId;
    private String driverName;
    private String driverPhone;
    private String vehicleNumber;
    private String notes;
}