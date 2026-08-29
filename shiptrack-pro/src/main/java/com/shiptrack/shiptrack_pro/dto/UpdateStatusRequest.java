package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotBlank
    private String status;
}