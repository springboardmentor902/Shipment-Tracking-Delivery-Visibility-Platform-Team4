package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProofOfDeliveryResponse {

    private Long id;

    private Long shipmentId;

    private String trackingNumber;

    private String signatureUrl;

    private String photoUrl;

    private String deliveredTo;

    private String deliveryNotes;

    private String verificationStatus;

    private String verifiedBy;

    private LocalDateTime deliveredAt;
}