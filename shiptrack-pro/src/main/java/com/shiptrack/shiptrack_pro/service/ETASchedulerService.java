package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ETASchedulerService {

    private final ShipmentRepository shipmentRepository;
    private final ETAService etaService;

    /*
     * Recalculate ETA every 15 minutes.
     */
    @Scheduled(fixedRate = 900000)
    public void recalculateActiveShipmentETAs() {

        List<Shipment> shipments =
                shipmentRepository.findByStatusIn(
                        List.of(
                                ShipmentStatus.PICKED_UP,
                                ShipmentStatus.IN_TRANSIT,
                                ShipmentStatus.OUT_FOR_DELIVERY
                        )
                );

        for (Shipment shipment : shipments) {

            try {
                etaService.predictETA(shipment.getId());
            } catch (Exception e) {
                // One failed shipment should not stop
                // ETA calculation for other shipments.
                System.err.println(
                        "ETA recalculation failed for shipment "
                                + shipment.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }
    }
}