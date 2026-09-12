package com.shiptrack.shiptrack_pro.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePerformanceReportRow {

    private String trackingNumber;
    private String origin;
    private String destination;
    private Double distanceKm;
    private Integer estimatedDurationMinutes;
    private Long actualDurationMinutes;   // null if shipment not yet delivered / missing events
}