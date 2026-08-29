package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "shipment_id",
            nullable = false
    )
    private Shipment shipment;

    @Column(nullable = false)
    private String description;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(name = "length_cm")
    private Double lengthCm;

    @Column(name = "width_cm")
    private Double widthCm;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "declared_value")
    private Double declaredValue;

    @Column(nullable = false)
    private Boolean fragile;
}