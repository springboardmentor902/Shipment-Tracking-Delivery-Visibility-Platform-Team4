package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "proof_of_delivery")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "delivered_to", nullable = false)
    private String deliveredTo;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}