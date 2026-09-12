package com.shiptrack.shiptrack_pro.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentReportRow {

    private String trackingNumber;
    private String status;
    private String origin;
    private String destination;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDelivery;
}