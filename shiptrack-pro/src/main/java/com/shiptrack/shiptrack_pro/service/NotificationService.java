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
    // CREATE NOTIFICATION
    // =========================================================

    public NotificationResponse createNotification(
            NotificationRequest request) {

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );


        // =====================================================
        // SAVE NOTIFICATION FOR FRONTEND BELL
        // =====================================================

        Notification notification = Notification.builder()
                .user(user)
                .message(request.getMessage())
                .notificationType(request.getNotificationType())
                .shipmentId(request.getShipmentId())
                .read(false)
                .build();

        Notification saved =
                notificationRepository.save(notification);


        // =====================================================
        // SEND EMAIL
        // =====================================================

        try {

            String emailMessage =
                    buildDetailedEmail(request);

            emailService.sendNotificationEmail(
                    user.getEmail(),
                    "ShipTrack Pro - Shipment Notification",
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


        // =====================================================
        // SEND SMS ONLY FOR DELAY WARNING
        // =====================================================

        if ("DELAY_WARNING".equalsIgnoreCase(
                request.getNotificationType())) {

            if (user.getPhone() != null &&
                    !user.getPhone().isBlank()) {

                try {

                    twilioService.sendSms(
                            user.getPhone(),
                            request.getMessage()
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
            }
        }

        return toResponse(saved);
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
                "You have received a shipment notification.\n\n"
        );


        // =====================================================
        // NOTIFICATION
        // =====================================================

        email.append(
                "NOTIFICATION\n"
        );

        email.append(
                "--------------------------------------------------\n"
        );

        email.append(
                "Type: "
        );

        email.append(
                request.getNotificationType() != null
                        ? request.getNotificationType()
                        : "GENERAL"
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
                            .findById(request.getShipmentId())
                            .orElse(null);

            if (shipment != null) {

                email.append(
                        "SHIPMENT DETAILS\n"
                );

                email.append(
                        "--------------------------------------------------\n"
                );


                // Shipment ID
                email.append(
                        "Shipment ID: "
                );

                email.append(
                        shipment.getId()
                );

                email.append("\n");


                // Tracking Number
                email.append(
                        "Tracking Number: "
                );

                email.append(
                        shipment.getTrackingNumber()
                );

                email.append("\n");


                // Sender
                email.append(
                        "Sender: "
                );

                email.append(
                        shipment.getSender()
                );

                email.append("\n");


                // Receiver
                email.append(
                        "Receiver: "
                );

                email.append(
                        shipment.getReceiver()
                );

                email.append("\n");


                // Origin
                email.append(
                        "Origin: "
                );

                email.append(
                        shipment.getOrigin()
                );

                email.append("\n");


                // Destination
                email.append(
                        "Destination: "
                );

                email.append(
                        shipment.getDestination()
                );

                email.append("\n");


                // Current Location
                email.append(
                        "Current Location: "
                );

                email.append(
                        shipment.getCurrentLocation() != null
                                ? shipment.getCurrentLocation()
                                : "Not available"
                );

                email.append("\n");


                // Status
                email.append(
                        "Status: "
                );

                email.append(
                        shipment.getStatus() != null
                                ? shipment.getStatus()
                                : "Not available"
                );

                email.append("\n");


                // Estimated Delivery
                email.append(
                        "Estimated Delivery: "
                );

                if (shipment.getEstimatedDelivery() != null) {

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern(
                                    "dd MMM yyyy, hh:mm a"
                            );

                    email.append(
                            shipment.getEstimatedDelivery()
                                    .format(formatter)
                    );

                } else {

                    email.append(
                            "Not available"
                    );
                }

                email.append("\n");


                // Created At
                email.append(
                        "Created At: "
                );

                if (shipment.getCreatedAt() != null) {

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern(
                                    "dd MMM yyyy, hh:mm a"
                            );

                    email.append(
                            shipment.getCreatedAt()
                                    .format(formatter)
                    );

                } else {

                    email.append(
                            "Not available"
                    );
                }

                email.append("\n");


                // Updated At
                email.append(
                        "Last Updated: "
                );

                if (shipment.getUpdatedAt() != null) {

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern(
                                    "dd MMM yyyy, hh:mm a"
                            );

                    email.append(
                            shipment.getUpdatedAt()
                                    .format(formatter)
                    );

                } else {

                    email.append(
                            "Not available"
                    );
                }

                email.append("\n\n");

            } else {

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
                "Thank you for using ShipTrack Pro.\n"
        );

        email.append(
                "This is an automated notification. "
                        + "Please do not reply to this email.\n"
        );


        return email.toString();
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
                notificationRepository.save(notification);

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