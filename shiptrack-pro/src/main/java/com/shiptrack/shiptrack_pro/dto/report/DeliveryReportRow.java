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
public class DeliveryReportRow {

    private String trackingNumber;
    private LocalDateTime actualDeliveryDate;   // from ProofOfDelivery.deliveredAt, may be null
    private String verificationStatus;          // from ProofOfDelivery, "NOT_RECORDED" if missing
    private String deliveredTo;
}