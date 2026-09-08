package com.shiptrack.shiptrack_pro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrack.shiptrack_pro.entity.User;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final ObjectMapper objectMapper;

    @Value("${vapid.public-key}")
    private String vapidPublicKey;

    @Value("${vapid.private-key}")
    private String vapidPrivateKey;

    @Value("${vapid.subject}")
    private String vapidSubject;


    public void sendPush(
            User user,
            String title,
            String message) {

        if (user == null) {
            return;
        }

        if (user.getPushEndpoint() == null ||
                user.getPushEndpoint().isBlank()) {

            System.out.println(
                    "No push subscription found for user: "
                            + user.getId()
            );

            return;
        }

        try {

            String payload = objectMapper.writeValueAsString(
                    new PushPayload(title, message)
            );

            PushService pushService =
                    new PushService(
                            vapidPublicKey,
                            vapidPrivateKey,
                            vapidSubject
                    );

            Notification notification =
                    new Notification(
                            user.getPushEndpoint(),
                            user.getPushP256dh(),
                            user.getPushAuth(),
                            payload
                    );

            pushService.send(notification);

            System.out.println(
                    "Push notification sent successfully to user: "
                            + user.getId()
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to send push notification: "
                            + e.getMessage()
            );
        }
    }


    private record PushPayload(
            String title,
            String message
    ) {
    }
}