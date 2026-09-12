package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import com.shiptrack.shiptrack_pro.service.ProofOfDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pod")
@RequiredArgsConstructor
public class ProofOfDeliveryController {

    private final ProofOfDeliveryService podService;

    @PostMapping(
            value = "/shipments/{shipmentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProofOfDeliveryResponse>
    createProofOfDelivery(

            @PathVariable Long shipmentId,

            @RequestParam("deliveredTo")
            String deliveredTo,

            @RequestParam(
                    value = "deliveryNotes",
                    required = false
            )
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

        String userEmail =
                authentication.getName();

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

    /*
     * Frontend GET:
     * /api/pod/{shipmentId}
     */
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ProofOfDeliveryResponse>
    getProofOfDelivery(
            @PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                podService.getProofOfDelivery(shipmentId)
        );
    }

    /*
     * Pending verification queue
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ProofOfDeliveryResponse>>
    getPendingProofsOfDelivery() {

        return ResponseEntity.ok(
                podService.getPendingProofsOfDelivery()
        );
    }

    /*
     * Frontend PATCH:
     * /api/pod/{shipmentId}/verify
     *
     * Body:
     * {
     *   "verified": true,
     *   "rejectionReason": ""
     * }
     */
    @PatchMapping("/{shipmentId}/verify")
    public ResponseEntity<ProofOfDeliveryResponse>
    verifyProofOfDelivery(

            @PathVariable Long shipmentId,

            @RequestBody VerifyPODRequest request,

            Authentication authentication) {

        String verifierEmail =
                authentication.getName();

        return ResponseEntity.ok(
                podService.verifyProofOfDelivery(
                        shipmentId,
                        request.isVerified(),
                        request.getRejectionReason(),
                        verifierEmail
                )
        );
    }

    /*
     * Small request DTO used only by this controller.
     */
    public static class VerifyPODRequest {

        private boolean verified;

        private String rejectionReason;

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }
}