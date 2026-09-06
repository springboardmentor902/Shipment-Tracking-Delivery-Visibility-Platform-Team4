package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.NotificationRequest;
import com.shiptrack.shiptrack_pro.dto.NotificationResponse;
import com.shiptrack.shiptrack_pro.entity.Notification;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.NotificationRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final EmailService emailService;
    private final TwilioService twilioService;


    // =========================================================
    // DATE FORMATTER
    // =========================================================

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    public NotificationResponse createNotification(
            NotificationRequest request) {

        // -----------------------------------------------------
        // FIND USER
        // -----------------------------------------------------

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );


        // -----------------------------------------------------
        // SAVE NOTIFICATION
        // -----------------------------------------------------

        Notification notification = Notification.builder()
                .user(user)
                .message(request.getMessage())
                .notificationType(request.getNotificationType())
                .shipmentId(request.getShipmentId())
                .read(false)
                .build();

        Notification saved =
                notificationRepository.save(notification);


        // -----------------------------------------------------
        // SEND EMAIL
        // -----------------------------------------------------

        if (user.getEmail() != null &&
                !user.getEmail().isBlank()) {

            try {

                String emailMessage =
                        buildDetailedEmail(request);

                emailService.sendNotificationEmail(
                        user.getEmail(),
                        buildEmailSubject(request),
                        emailMessage
                );

                System.out.println(
                        "Notification email sent successfully to "
                                + user.getEmail()
                );

            } catch (Exception e) {

                System.err.println(
                        "Failed to send notification email to "
                                + user.getEmail()
                                + ": "
                                + e.getMessage()
                );
            }

        } else {

            System.out.println(
                    "User does not have a valid email address."
            );
        }


        // -----------------------------------------------------
        // SEND SMS ONLY FOR DELAY WARNING
        // -----------------------------------------------------

        if ("DELAY_WARNING".equalsIgnoreCase(
                request.getNotificationType())) {

            if (user.getPhone() != null &&
                    !user.getPhone().isBlank()) {

                try {

                    twilioService.sendSms(
                            user.getPhone(),
                            buildDelaySmsMessage(request)
                    );

                    System.out.println(
                            "Delay warning SMS sent successfully to "
                                    + user.getPhone()
                    );

                } catch (Exception e) {

                    System.err.println(
                            "Failed to send delay warning SMS to "
                                    + user.getPhone()
                                    + ": "
                                    + e.getMessage()
                    );
                }

            } else {

                System.out.println(
                        "User does not have a valid phone number."
                );
            }
        }


        return toResponse(saved);
    }


    // =========================================================
    // BUILD EMAIL SUBJECT
    // =========================================================

    private String buildEmailSubject(
            NotificationRequest request) {

        String notificationType =
                request.getNotificationType();

        if (notificationType == null ||
                notificationType.isBlank()) {

            return "ShipTrack Pro - Shipment Notification";
        }

        return "ShipTrack Pro - "
                + formatNotificationType(notificationType);
    }


    // =========================================================
    // BUILD DETAILED EMAIL
    // =========================================================

    private String buildDetailedEmail(
            NotificationRequest request) {

        StringBuilder email = new StringBuilder();


        // =====================================================
        // HEADER
        // =====================================================

        email.append(
                "SHIPTRACK PRO\n"
        );

        email.append(
                "Shipment Tracking & Delivery Visibility Platform\n"
        );

        email.append(
                "==================================================\n\n"
        );


        email.append(
                "Hello,\n\n"
        );

        email.append(
                "You have received a shipment notification "
                        + "from ShipTrack Pro.\n\n"
        );


        // =====================================================
        // NOTIFICATION DETAILS
        // =====================================================

        email.append(
                "NOTIFICATION DETAILS\n"
        );

        email.append(
                "--------------------------------------------------\n"
        );


        email.append(
                "Notification Type: "
        );

        email.append(
                request.getNotificationType() != null
                        ? formatNotificationType(
                        request.getNotificationType()
                )
                        : "General"
        );

        email.append("\n");


        email.append(
                "Message: "
        );

        email.append(
                request.getMessage() != null
                        ? request.getMessage()
                        : "Shipment notification"
        );

        email.append("\n\n");


        // =====================================================
        // SHIPMENT DETAILS
        // =====================================================

        if (request.getShipmentId() != null) {

            Shipment shipment =
                    shipmentRepository
                            .findById(
                                    request.getShipmentId()
                            )
                            .orElse(null);


            if (shipment != null) {

                email.append(
                        "SHIPMENT DETAILS\n"
                );

                email.append(
                        "--------------------------------------------------\n"
                );


                // Shipment ID
                appendField(
                        email,
                        "Shipment ID",
                        shipment.getId()
                );


                // Tracking Number
                appendField(
                        email,
                        "Tracking Number",
                        shipment.getTrackingNumber()
                );


                // Sender
                appendField(
                        email,
                        "Sender",
                        shipment.getSender()
                );


                // Receiver
                appendField(
                        email,
                        "Receiver",
                        shipment.getReceiver()
                );


                // Origin
                appendField(
                        email,
                        "Origin",
                        shipment.getOrigin()
                );


                // Destination
                appendField(
                        email,
                        "Destination",
                        shipment.getDestination()
                );


                // Current Location
                appendField(
                        email,
                        "Current Location",
                        shipment.getCurrentLocation()
                );


                // Status
                appendField(
                        email,
                        "Status",
                        shipment.getStatus()
                );


                // Estimated Delivery
                appendDateField(
                        email,
                        "Estimated Delivery",
                        shipment.getEstimatedDelivery()
                );


                // Created At
                appendDateField(
                        email,
                        "Created At",
                        shipment.getCreatedAt()
                );


                // Updated At
                appendDateField(
                        email,
                        "Last Updated",
                        shipment.getUpdatedAt()
                );


                email.append("\n");


            } else {

                email.append(
                        "SHIPMENT DETAILS\n"
                );

                email.append(
                        "--------------------------------------------------\n"
                );

                email.append(
                        "Shipment details are currently unavailable.\n\n"
                );
            }

        } else {

            email.append(
                    "Shipment ID: Not provided\n\n"
            );
        }


        // =====================================================
        // FOOTER
        // =====================================================

        email.append(
                "==================================================\n"
        );

        email.append(
                "Thank you for using ShipTrack Pro.\n\n"
        );

        email.append(
                "This is an automated notification from "
                        + "ShipTrack Pro.\n"
        );

        email.append(
                "Please do not reply directly to this email.\n"
        );


        return email.toString();
    }


    // =========================================================
    // BUILD DELAY SMS
    // =========================================================

    private String buildDelaySmsMessage(
            NotificationRequest request) {

        StringBuilder sms =
                new StringBuilder();

        sms.append(
                "ShipTrack Pro - Delay Warning\n"
        );

        sms.append(
                request.getMessage() != null
                        ? request.getMessage()
                        : "Your shipment may be delayed."
        );


        if (request.getShipmentId() != null) {

            Shipment shipment =
                    shipmentRepository
                            .findById(
                                    request.getShipmentId()
                            )
                            .orElse(null);

            if (shipment != null) {

                sms.append(
                        "\nTracking: "
                );

                sms.append(
                        shipment.getTrackingNumber() != null
                                ? shipment.getTrackingNumber()
                                : "N/A"
                );

                sms.append(
                        "\nCurrent Location: "
                );

                sms.append(
                        shipment.getCurrentLocation() != null
                                ? shipment.getCurrentLocation()
                                : "N/A"
                );

                sms.append(
                        "\nDestination: "
                );

                sms.append(
                        shipment.getDestination() != null
                                ? shipment.getDestination()
                                : "N/A"
                );
            }
        }


        return sms.toString();
    }


    // =========================================================
    // APPEND NORMAL FIELD
    // =========================================================

    private void appendField(
            StringBuilder email,
            String fieldName,
            Object value) {

        email.append(fieldName);
        email.append(": ");

        if (value != null &&
                !value.toString().isBlank()) {

            email.append(value);

        } else {

            email.append("Not available");
        }

        email.append("\n");
    }


    // =========================================================
    // APPEND DATE FIELD
    // =========================================================

    private void appendDateField(
            StringBuilder email,
            String fieldName,
            java.time.LocalDateTime value) {

        email.append(fieldName);
        email.append(": ");

        if (value != null) {

            email.append(
                    value.format(DATE_FORMATTER)
            );

        } else {

            email.append(
                    "Not available"
            );
        }

        email.append("\n");
    }


    // =========================================================
    // FORMAT NOTIFICATION TYPE
    // =========================================================

    private String formatNotificationType(
            String notificationType) {

        if (notificationType == null ||
                notificationType.isBlank()) {

            return "General";
        }

        String[] words =
                notificationType
                        .replace("_", " ")
                        .toLowerCase()
                        .split(" ");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {

            if (word.isEmpty()) {
                continue;
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            if (word.length() > 1) {

                result.append(
                        word.substring(1)
                );
            }

            result.append(" ");
        }

        return result.toString().trim();
    }


    // =========================================================
    // GET USER NOTIFICATIONS
    // =========================================================

    public List<NotificationResponse> getUserNotifications(
            Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // MARK NOTIFICATION AS READ
    // =========================================================

    public NotificationResponse markAsRead(
            Long notificationId,
            Long userId) {

        Notification notification =
                notificationRepository
                        .findByIdAndUserId(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );


        notification.setRead(true);


        Notification updated =
                notificationRepository.save(
                        notification
                );


        return toResponse(updated);
    }


    // =========================================================
    // CONVERT ENTITY TO RESPONSE
    // =========================================================

    private NotificationResponse toResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .notificationType(
                        notification.getNotificationType()
                )
                .shipmentId(
                        notification.getShipmentId()
                )
                .read(
                        notification.isRead()
                )
                .createdAt(
                        notification.getCreatedAt()
                )
                .build();
    }
}