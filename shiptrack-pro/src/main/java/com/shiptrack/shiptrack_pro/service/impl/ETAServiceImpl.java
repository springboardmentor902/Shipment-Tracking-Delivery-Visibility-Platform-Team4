package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.entity.ETAPrediction;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.repository.ETAPredictionRepository;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.TrackingEventRepository;
import com.shiptrack.shiptrack_pro.service.ETAService;
import com.shiptrack.shiptrack_pro.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ETAServiceImpl implements ETAService {

    private final ETAPredictionRepository etaPredictionRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ETAPrediction predictETA(Long shipmentId) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Shipment not found with id: " + shipmentId
                        )
                );

        Route route = routeRepository
                .findByShipmentIdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No route found for shipment: " + shipmentId
                        )
                );

        List<TrackingEvent> events =
                trackingEventRepository
                        .findByShipmentIdOrderByEventTimestampDesc(
                                shipmentId
                        );

        LocalDateTime calculatedAt = LocalDateTime.now();

        Integer estimatedMinutes =
                route.getEstimatedDurationMinutes();

        if (estimatedMinutes == null || estimatedMinutes < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Route does not contain a valid estimated duration"
            );
        }

        LocalDateTime predictedDeliveryTime =
                calculatedAt.plusMinutes(estimatedMinutes);

        double delayRiskScore = 0.0;

        StringBuilder factors = new StringBuilder();

        String trafficCondition = getTrafficCondition(route);

        switch (trafficCondition) {

            case "HIGH":
                delayRiskScore += 3;
                factors.append("High traffic (+3); ");
                break;

            case "MEDIUM":
                delayRiskScore += 2;
                factors.append("Medium traffic (+2); ");
                break;

            default:
                factors.append("Low/normal traffic (+0); ");
                break;
        }

        if (events.isEmpty()) {

            delayRiskScore += 2;
            factors.append("No tracking history (+2); ");

        } else {

            TrackingEvent latestEvent = events.get(0);

            if (latestEvent.getStatus() != null &&
                    "DELAYED".equalsIgnoreCase(
                            latestEvent.getStatus().name()
                    )) {

                delayRiskScore += 4;
                factors.append(
                        "Latest tracking event indicates delay (+4); "
                );

            } else {

                factors.append(
                        "Tracking history indicates normal progress (+0); "
                );
            }
        }

        if (route.getDistanceKm() != null &&
                route.getDistanceKm() > 500) {

            delayRiskScore += 1;
            factors.append("Long route distance (+1); ");
        }

        if (shipment.getStatus() != null &&
                "DELIVERED".equalsIgnoreCase(
                        shipment.getStatus().name()
                )) {

            delayRiskScore = 0;
            predictedDeliveryTime = calculatedAt;

            factors.append("Shipment already delivered; ");
        }

        delayRiskScore =
                Math.max(0, Math.min(10, delayRiskScore));

        double confidenceScore;

        if (events.isEmpty()) {
            confidenceScore = 60.0;
        } else if (events.size() == 1) {
            confidenceScore = 70.0;
        } else if (events.size() <= 3) {
            confidenceScore = 80.0;
        } else {
            confidenceScore = 90.0;
        }

        if (route.getDistanceKm() == null ||
                route.getEstimatedDurationMinutes() == null) {

            confidenceScore -= 20.0;
        }

        confidenceScore =
                Math.max(0, Math.min(100, confidenceScore));

        Double previousScore = etaPredictionRepository
                .findByShipmentId(shipmentId)
                .map(ETAPrediction::getDelayRiskScore)
                .orElse(0.0);

        ETAPrediction prediction =
                etaPredictionRepository
                        .findByShipmentId(shipmentId)
                        .orElse(
                                ETAPrediction.builder()
                                        .shipment(shipment)
                                        .build()
                        );

        prediction.setShipment(shipment);

        prediction.setPredictedDeliveryTime(
                predictedDeliveryTime
        );

        prediction.setDelayRiskScore(
                delayRiskScore
        );

        prediction.setConfidenceScore(
                confidenceScore
        );

        prediction.setFactors(
                factors.toString()
        );

        prediction.setCalculatedAt(
                calculatedAt
        );

        ETAPrediction saved = etaPredictionRepository.save(prediction);

        double threshold = 7.0;
        if (previousScore < threshold && delayRiskScore >= threshold) {
            notificationService.send("DELAY_WARNING", shipment.getUser(), shipment);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ETAPrediction getPrediction(Long shipmentId) {

        if (!shipmentRepository.existsById(shipmentId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found with id: " + shipmentId
            );
        }

        return etaPredictionRepository
                .findByShipmentId(shipmentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "ETA prediction not found for shipment: "
                                        + shipmentId
                        )
                );
    }

    private String getTrafficCondition(Route route) {

        if (route.getTrafficCondition() == null ||
                route.getTrafficCondition().isBlank()) {

            return "LOW";
        }

        return route.getTrafficCondition()
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}