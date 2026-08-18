package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessAccRequest {

    @NotBlank
    private String businessName;

    private String businessEmail;

    private String businessPhone;

    private String businessAddress;

    private String taxId;
}