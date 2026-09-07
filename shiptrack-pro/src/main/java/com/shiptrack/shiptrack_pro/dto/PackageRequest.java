package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @Positive(message = "Weight must be positive")
    private Double weight;

    @Positive(message = "Length must be positive")
    private Double lengthCm;

    @Positive(message = "Width must be positive")
    private Double widthCm;

    @Positive(message = "Height must be positive")
    private Double heightCm;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @Positive(message = "Declared value must be positive")
    private Double declaredValue;

    private Boolean isFragile;
    private String trackingNumber;
    private String notes;
}