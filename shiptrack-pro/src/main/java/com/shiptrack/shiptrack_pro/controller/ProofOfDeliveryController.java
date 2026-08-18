package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import com.shiptrack.shiptrack_pro.service.ProofOfDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pod")
@RequiredArgsConstructor
public class ProofOfDeliveryController {

    private final ProofOfDeliveryService podService;

    @PostMapping(
            value = "/shipments/{shipmentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProofOfDeliveryResponse> createProofOfDelivery(

            @PathVariable Long shipmentId,

            @RequestParam("deliveredTo")
            String deliveredTo,

            @RequestParam(value = "deliveryNotes", required = false)
            String deliveryNotes,

            @RequestPart("signature")
            MultipartFile signature,

            @RequestPart("photo")
            MultipartFile photo,

            Authentication authentication) {

        ProofOfDeliveryRequest request =
                new ProofOfDeliveryRequest();

        request.setDeliveredTo(deliveredTo);
        request.setDeliveryNotes(deliveryNotes);

        String userEmail = authentication.getName();

        ProofOfDeliveryResponse response =
                podService.createProofOfDelivery(
                        shipmentId,
                        request,
                        signature,
                        photo,
                        userEmail
                );

        return ResponseEntity
                .status(201)
                .body(response);
    }

    @GetMapping("/shipments/{shipmentId}")
    public ResponseEntity<ProofOfDeliveryResponse>
    getProofOfDelivery(
            @PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                podService.getProofOfDelivery(shipmentId)
        );
    }
}