package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusinessAccResponse {

    private Long id;

    private Long userId;

    private String businessName;

    private String businessEmail;

    private String businessPhone;

    private String businessAddress;

    private String taxId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}