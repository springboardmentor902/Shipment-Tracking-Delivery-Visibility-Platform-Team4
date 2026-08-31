package com.shiptrack.shiptrack_pro.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private Long userId;

    private String message;

    private String notificationType;

    private Long shipmentId;
}