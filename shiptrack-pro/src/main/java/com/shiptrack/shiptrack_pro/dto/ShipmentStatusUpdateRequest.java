package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private Shipment.ShipmentStatus status;

    private String notes;
    private String location;
}