package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProofOfDeliveryRequest {

    @NotBlank(message = "Delivered-to name is required")
    private String deliveredTo;

    private String deliveryNotes;
}