package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccountRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must be less than 100 characters")
    private String companyName;

    @Size(max = 50, message = "GST number must be less than 50 characters")
    private String gstNumber;

    @Size(max = 255, message = "Business address must be less than 255 characters")
    private String businessAddress;

    @Size(max = 50, message = "Industry type must be less than 50 characters")
    private String industryType;

    @Size(max = 20, message = "Business phone must be less than 20 characters")
    private String businessPhone;

    @Size(max = 100, message = "Business email must be less than 100 characters")
    private String businessEmail;

    @Size(max = 100, message = "Website must be less than 100 characters")
    private String website;

    @Size(max = 20, message = "PAN number must be less than 20 characters")
    private String panNumber;

    @Size(max = 50, message = "Registration number must be less than 50 characters")
    private String registrationNumber;

    @Size(max = 50, message = "Tax ID must be less than 50 characters")
    private String taxId;

    private String businessType;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;
}