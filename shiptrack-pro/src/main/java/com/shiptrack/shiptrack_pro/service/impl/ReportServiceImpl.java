package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.report.*;
import com.shiptrack.shiptrack_pro.entity.*;
import com.shiptrack.shiptrack_pro.repository.*;
import com.shiptrack.shiptrack_pro.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ShipmentRepository shipmentRepository;
    private final ProofOfDeliveryRepository proofOfDeliveryRepository;
    private final RouteRepository routeRepository;
    private final ETAPredictionRepository etaPredictionRepository;
    private final TrackingEventRepository trackingEventRepository;

    // Threshold for "at risk" shipments in the Delay Analysis report.
    // Matches the DELAY_WARNING notification threshold — not yet
    // formally agreed with the team, same as flagged earlier.
    private static final double DELAY_RISK_THRESHOLD = 7.0;


    // =====================================================
    // HELPER — is this user an admin (sees everything)?
    // =====================================================

    private boolean isAdmin(User user) {
        return "ADMINISTRATOR".equalsIgnoreCase(user.getRole());
    }


    // =====================================================
    // 1. SHIPMENT REPORT
    // =====================================================

    @Override
    public List<ShipmentReportRow> getShipmentReport(User currentUser) {

        List<Shipment> shipments =
                isAdmin(currentUser)
                        ? shipmentRepository.findAll()
                        : shipmentRepository.findByUserId(currentUser.getId());

        return shipments.stream()
                .map(this::mapToShipmentReportRow)
                .collect(Collectors.toList());
    }

    private ShipmentReportRow mapToShipmentReportRow(Shipment shipment) {

        return ShipmentReportRow.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .status(
                        shipment.getStatus() == null
                                ? null
                                : shipment.getStatus().name()
                )
                .origin(shipment.getOrigin())
                .destination(shipment.getDestination())
                .createdAt(shipment.getCreatedAt())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .build();
    }


    // =====================================================
    // 2. DELIVERY REPORT
    // =====================================================

    @Override
    public List<DeliveryReportRow> getDeliveryReport(User currentUser) {

        List<Shipment> deliveredShipments =
                isAdmin(currentUser)
                        ? shipmentRepository.findByStatusIn(
                        List.of(ShipmentStatus.DELIVERED))
                        : shipmentRepository.findByUserIdAndStatus(
                        currentUser.getId(),
                        ShipmentStatus.DELIVERED);

        return deliveredShipments.stream()
                .map(this::mapToDeliveryReportRow)
                .collect(Collectors.toList());
    }

    private DeliveryReportRow mapToDeliveryReportRow(Shipment shipment) {

        Optional<ProofOfDelivery> pod =
                proofOfDeliveryRepository.findByShipmentId(shipment.getId());

        return DeliveryReportRow.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .actualDeliveryDate(
                        pod.map(ProofOfDelivery::getDeliveredAt)
                                .orElse(null)
                )
                .verificationStatus(
                        pod.map(ProofOfDelivery::getVerificationStatus)
                                .orElse("NOT_RECORDED")
                )
                .deliveredTo(
                        pod.map(ProofOfDelivery::getDeliveredTo)
                                .orElse("NOT_RECORDED")
                )
                .build();
    }


    // =====================================================
    // 3. ROUTE PERFORMANCE REPORT
    // =====================================================

    @Override
    public List<RoutePerformanceReportRow> getRoutePerformanceReport(User currentUser) {

        List<Shipment> shipments =
                isAdmin(currentUser)
                        ? shipmentRepository.findAll()
                        : shipmentRepository.findByUserId(currentUser.getId());

        return shipments.stream()
                .map(this::mapToRoutePerformanceRow)
                .filter(row -> row != null)
                .collect(Collectors.toList());
    }

    private RoutePerformanceReportRow mapToRoutePerformanceRow(Shipment shipment) {

        List<Route> routes =
        routeRepository.findByShipmentIdOrderByCreatedAtDesc(
                shipment.getId()
        );

// No route created for this shipment yet — skip it
if (routes.isEmpty()) {
    return null;
}

// Most recent route
Route route = routes.get(0);

        return RoutePerformanceReportRow.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .distanceKm(route.getDistanceKm())
                .estimatedDurationMinutes(route.getEstimatedDurationMinutes())
                .actualDurationMinutes(
                        computeActualDurationMinutes(shipment.getId())
                )
                .build();
    }

    // Derives "actual" transit time from tracking event timestamps:
    // time between the PICKED_UP event and the DELIVERED event.
    // Returns null if the shipment hasn't reached DELIVERED yet,
    // or if one of the two events is missing.
    private Long computeActualDurationMinutes(Long shipmentId) {

        List<TrackingEvent> events =
                trackingEventRepository
                        .findByShipmentIdOrderByEventTimestampDesc(shipmentId);

        TrackingEvent pickedUpEvent = events.stream()
                .filter(e -> e.getStatus() == ShipmentStatus.PICKED_UP)
                .findFirst()
                .orElse(null);

        TrackingEvent deliveredEvent = events.stream()
                .filter(e -> e.getStatus() == ShipmentStatus.DELIVERED)
                .findFirst()
                .orElse(null);

        if (pickedUpEvent == null || deliveredEvent == null) {
            return null;
        }

        return Duration.between(
                pickedUpEvent.getEventTimestamp(),
                deliveredEvent.getEventTimestamp()
        ).toMinutes();
    }


    // =====================================================
    // 4. DELAY ANALYSIS REPORT
    // =====================================================

    @Override
    public List<DelayAnalysisReportRow> getDelayAnalysisReport(User currentUser) {

        List<ETAPrediction> predictions =
                etaPredictionRepository
                        .findByDelayRiskScoreGreaterThanEqual(DELAY_RISK_THRESHOLD);

        // Non-admins only see predictions for their own shipments
        if (!isAdmin(currentUser)) {
            predictions = predictions.stream()
                    .filter(p ->
                            p.getShipment() != null
                                    && p.getShipment().getUser() != null
                                    && p.getShipment().getUser().getId()
                                    .equals(currentUser.getId())
                    )
                    .collect(Collectors.toList());
        }

        return predictions.stream()
                .map(this::mapToDelayAnalysisRow)
                .collect(Collectors.toList());
    }

    private DelayAnalysisReportRow mapToDelayAnalysisRow(ETAPrediction prediction) {

        return DelayAnalysisReportRow.builder()
                .trackingNumber(
                        prediction.getShipment() == null
                                ? null
                                : prediction.getShipment().getTrackingNumber()
                )
                .delayRiskScore(prediction.getDelayRiskScore())
                .predictedDeliveryTime(prediction.getPredictedDeliveryTime())
                .factors(prediction.getFactors())
                .build();
    }
}