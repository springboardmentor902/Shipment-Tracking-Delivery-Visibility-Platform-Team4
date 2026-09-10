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
public class DelayAnalysisReportRow {

    private String trackingNumber;
    private Double delayRiskScore;
    private LocalDateTime predictedDeliveryTime;
    private String factors;
}