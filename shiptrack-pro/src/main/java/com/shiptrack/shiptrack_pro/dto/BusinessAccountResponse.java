package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.BusinessAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccountResponse {

    private Long id;
    private Long userId;
    private String companyName;
    private String gstNumber;
    private String businessAddress;
    private String industryType;
    private String businessPhone;
    private String businessEmail;
    private String website;
    private String panNumber;
    private String registrationNumber;
    private String taxId;
    private Boolean isVerified;
    private LocalDateTime verificationDate;
    private String businessType;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BusinessAccountResponse fromEntity(BusinessAccount account) {
        if (account == null) return null;

        return BusinessAccountResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .companyName(account.getCompanyName())
                .gstNumber(account.getGstNumber())
                .businessAddress(account.getBusinessAddress())
                .industryType(account.getIndustryType())
                .businessPhone(account.getBusinessPhone())
                .businessEmail(account.getBusinessEmail())
                .website(account.getWebsite())
                .panNumber(account.getPanNumber())
                .registrationNumber(account.getRegistrationNumber())
                .taxId(account.getTaxId())
                .isVerified(account.getIsVerified())
                .verificationDate(account.getVerificationDate())
                .businessType(account.getBusinessType() != null ?
                        account.getBusinessType().name() : null)
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}