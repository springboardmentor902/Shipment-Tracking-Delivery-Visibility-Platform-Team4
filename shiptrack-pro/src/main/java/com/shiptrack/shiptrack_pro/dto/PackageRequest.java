package com.shiptrack.shiptrack_pro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageRequest {

    private Long shipmentId;

    private String description;

    private Double weightKg;

    private Double lengthCm;

    private Double widthCm;

    private Double heightCm;

    private Integer quantity;

    private Double declaredValue;

    private Boolean fragile;
}