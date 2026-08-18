package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import com.shiptrack.shiptrack_pro.entity.ProofOfDelivery;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.ShipmentStatus;
import com.shiptrack.shiptrack_pro.repository.ProofOfDeliveryRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.service.ProofOfDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProofOfDeliveryServiceImpl
        implements ProofOfDeliveryService {

    private final ProofOfDeliveryRepository podRepository;
    private final ShipmentRepository shipmentRepository;

    private final Path uploadDirectory =
            Paths.get("uploads/pod");

    @Override
    public ProofOfDeliveryResponse createProofOfDelivery(
            Long shipmentId,
            ProofOfDeliveryRequest request,
            MultipartFile signature,
            MultipartFile photo,
            String userEmail) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Shipment not found with id: " + shipmentId
                        )
                );

        if (podRepository.existsByShipmentId(shipmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Proof of Delivery already exists for this shipment."
            );
        }

        try {
            Files.createDirectories(uploadDirectory);

            String signatureUrl =
                    saveFile(signature, "signature");

            String photoUrl =
                    saveFile(photo, "photo");

            ProofOfDelivery pod =
                    ProofOfDelivery.builder()
                            .shipment(shipment)
                            .signatureUrl(signatureUrl)
                            .photoUrl(photoUrl)
                            .deliveredTo(request.getDeliveredTo())
                            .deliveryNotes(request.getDeliveryNotes())
                            .verificationStatus("VERIFIED")
                            .verifiedBy(userEmail)
                            .deliveredAt(LocalDateTime.now())
                            .build();

            ProofOfDelivery saved =
                    podRepository.save(pod);

            // Update shipment automatically
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipmentRepository.save(shipment);

            return mapToResponse(saved);

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save proof of delivery files"
            );
        }
    }

    @Override
    public ProofOfDeliveryResponse getProofOfDelivery(
            Long shipmentId) {

        ProofOfDelivery pod =
                podRepository.findByShipmentId(shipmentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Proof of Delivery not found for shipment: "
                                                + shipmentId
                                )
                        );

        return mapToResponse(pod);
    }

    private String saveFile(
            MultipartFile file,
            String prefix)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    prefix + " file is required"
            );
        }

        String originalName =
                file.getOriginalFilename();

        String extension = "";

        if (originalName != null &&
                originalName.contains(".")) {

            extension =
                    originalName.substring(
                            originalName.lastIndexOf(".")
                    );
        }

        String fileName =
                prefix + "-" +
                        UUID.randomUUID() +
                        extension;

        Path filePath =
                uploadDirectory.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath
        );

        return "/uploads/pod/" + fileName;
    }

    private ProofOfDeliveryResponse mapToResponse(
            ProofOfDelivery pod) {

        return ProofOfDeliveryResponse.builder()
                .id(pod.getId())
                .shipmentId(
                        pod.getShipment().getId()
                )
                .trackingNumber(
                        pod.getShipment()
                                .getTrackingNumber()
                )
                .signatureUrl(
                        pod.getSignatureUrl()
                )
                .photoUrl(
                        pod.getPhotoUrl()
                )
                .deliveredTo(
                        pod.getDeliveredTo()
                )
                .deliveryNotes(
                        pod.getDeliveryNotes()
                )
                .verificationStatus(
                        pod.getVerificationStatus()
                )
                .verifiedBy(
                        pod.getVerifiedBy()
                )
                .deliveredAt(
                        pod.getDeliveredAt()
                )
                .build();
    }
}