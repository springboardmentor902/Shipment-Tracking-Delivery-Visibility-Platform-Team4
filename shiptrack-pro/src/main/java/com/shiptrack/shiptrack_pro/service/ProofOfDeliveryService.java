package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProofOfDeliveryService {

    ProofOfDeliveryResponse createProofOfDelivery(
            Long shipmentId,
            ProofOfDeliveryRequest request,
            MultipartFile signature,
            MultipartFile photo,
            String userEmail
    );

    ProofOfDeliveryResponse getProofOfDelivery(Long shipmentId);

    List<ProofOfDeliveryResponse> getPendingProofsOfDelivery();

    ProofOfDeliveryResponse verifyProofOfDelivery(
            Long shipmentId,
            boolean verified,
            String rejectionReason,
            String verifierEmail
    );
}