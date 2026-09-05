package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.entity.Notification;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.NotificationRepository;
import com.shiptrack.shiptrack_pro.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    private static final int DUPLICATE_WINDOW_MINUTES = 60;

    @Override
    public void send(String type, User user, Shipment shipment) {

        // ---- Duplicate prevention check ----
        Optional<Notification> lastNotification =
                notificationRepository.findTopByShipmentIdAndTypeOrderByCreatedAtDesc(
                        shipment.getId(), type
                );

        if (lastNotification.isPresent()) {
            LocalDateTime lastSentAt = lastNotification.get().getCreatedAt();
            if (lastSentAt.isAfter(LocalDateTime.now().minusMinutes(DUPLICATE_WINDOW_MINUTES))) {
                // Already notified recently for this shipment+type — skip
                return;
            }
        }

        // ---- Build notification content internally, based on type ----
        String title = buildTitle(type);
        String message = buildMessage(type, shipment);

        // ---- Create notification record (status PENDING until we know the outcome) ----
        Notification notification = Notification.builder()
                .user(user)
                .shipment(shipment)
                .title(title)
                .message(message)
                .type(type)
                .status("PENDING")
                .build();

        notificationRepository.save(notification);

        // ---- Attempt delivery ----
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(title);
            mail.setText(message);
            mailSender.send(mail);

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());

        } catch (Exception e) {
            notification.setStatus("FAILED");
        }

        notificationRepository.save(notification);
    }

    private String buildTitle(String type) {
        return switch (type) {
            case "DELAY_WARNING" -> "Delay Alert for Your Shipment";
            case "SHIPMENT_UPDATE" -> "Shipment Status Update";
            default -> "Shipment Notification";
        };
    }

    private String buildMessage(String type, Shipment shipment) {
        return switch (type) {
            case "DELAY_WARNING" -> "Your shipment " + shipment.getTrackingNumber()
                    + " is at risk of being delayed. We're monitoring it closely.";
            case "SHIPMENT_UPDATE" -> "Your shipment " + shipment.getTrackingNumber()
                    + " status has been updated to: " + shipment.getStatus();
            default -> "There is an update on your shipment " + shipment.getTrackingNumber() + ".";
        };
    }
}