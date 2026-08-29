package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "business_address")
    private String businessAddress;

    @Column(name = "industry_type")
    private String industryType;

    @Column(name = "business_phone")
    private String businessPhone;

    @Column(name = "business_email")
    private String businessEmail;

    @Column(name = "website")
    private String website;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "tax_id")
    private String taxId;

    // ✅ FIX: Add @Builder.Default for fields with default values
    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business Types
    public enum BusinessType {
        SOLE_PROPRIETORSHIP,
        PARTNERSHIP,
        PRIVATE_LIMITED,
        PUBLIC_LIMITED,
        LLP,
        TRUST,
        SOCIETY,
        OTHER
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type")
    private BusinessType businessType;
}