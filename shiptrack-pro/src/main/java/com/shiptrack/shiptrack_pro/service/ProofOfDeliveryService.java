package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProofOfDeliveryService {

    ProofOfDeliveryResponse createProofOfDelivery(
            Long shipmentId,
            ProofOfDeliveryRequest request,
            MultipartFile signature,
            MultipartFile photo,
            String userEmail
    );

    ProofOfDeliveryResponse getProofOfDelivery(Long shipmentId);
}