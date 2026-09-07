package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.Package;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {

    private Long id;
    private Long shipmentId;
    private String description;
    private Double weight;
    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;
    private Integer quantity;
    private Double declaredValue;
    private Boolean isFragile;
    private String trackingNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PackageResponse fromEntity(Package pkg) {
        if (pkg == null) return null;

        return PackageResponse.builder()
                .id(pkg.getId())
                .shipmentId(pkg.getShipmentId())
                .description(pkg.getDescription())
                .weight(pkg.getWeight())
                .lengthCm(pkg.getLengthCm())
                .widthCm(pkg.getWidthCm())
                .heightCm(pkg.getHeightCm())
                .quantity(pkg.getQuantity())
                .declaredValue(pkg.getDeclaredValue())
                .isFragile(pkg.getIsFragile())
                .trackingNumber(pkg.getTrackingNumber())
                .notes(pkg.getNotes())
                .createdAt(pkg.getCreatedAt())
                .updatedAt(pkg.getUpdatedAt())
                .build();
    }
}