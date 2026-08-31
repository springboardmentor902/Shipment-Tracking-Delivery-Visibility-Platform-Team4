package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;

    private String message;

    private String notificationType;

    private Long shipmentId;

    private boolean read;

    private LocalDateTime createdAt;
}