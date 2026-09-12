package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.LocationUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastLocation(Long shipmentId, LocationUpdateResponse locationUpdate) {
        String destination = "/topic/shipment/" + shipmentId;
        log.info("📡 Broadcasting location to {}: lat={}, lng={}",
                destination, locationUpdate.getLatitude(), locationUpdate.getLongitude());
        messagingTemplate.convertAndSend(destination, locationUpdate);
    }

    public void broadcastToUser(String username, Long shipmentId, LocationUpdateResponse locationUpdate) {
        String destination = "/user/" + username + "/queue/shipment/" + shipmentId;
        log.info("📡 Broadcasting location to user {} on channel: {}", username, destination);
        messagingTemplate.convertAndSendToUser(username, "/queue/shipment/" + shipmentId, locationUpdate);
    }

    public void broadcastDriverStatus(Long driverId, String status) {
        String destination = "/topic/driver/" + driverId;
        messagingTemplate.convertAndSend(destination, status);
    }
}