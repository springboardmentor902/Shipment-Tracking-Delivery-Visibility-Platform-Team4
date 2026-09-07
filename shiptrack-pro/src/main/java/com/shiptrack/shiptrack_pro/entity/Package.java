package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "length_cm")
    private Double lengthCm;

    @Column(name = "width_cm")
    private Double widthCm;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "declared_value")
    private Double declaredValue;

    @Column(name = "is_fragile")
    private Boolean isFragile;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}