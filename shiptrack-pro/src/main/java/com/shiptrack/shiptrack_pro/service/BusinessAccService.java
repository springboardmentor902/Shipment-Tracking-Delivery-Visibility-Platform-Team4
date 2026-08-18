package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.BusinessAccRequest;
import com.shiptrack.shiptrack_pro.dto.BusinessAccResponse;
import com.shiptrack.shiptrack_pro.entity.BusinessAcc;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.BusinessAccRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BusinessAccService {

    private final BusinessAccRepository businessAccRepository;
    private final UserRepository userRepository;

    // CREATE BUSINESS ACCOUNT
    public BusinessAccResponse createBusinessAccount(
            BusinessAccRequest request,
            String userEmail) {

        // Find logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // Check whether account already exists
        if (businessAccRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Business account already exists for this user"
            );
        }

        // Create business account
        BusinessAcc businessAcc = BusinessAcc.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessEmail(request.getBusinessEmail())
                .businessPhone(request.getBusinessPhone())
                .businessAddress(request.getBusinessAddress())
                .taxId(request.getTaxId())
                .build();

        // Save
        BusinessAcc saved =
                businessAccRepository.save(businessAcc);

        // Return response
        return mapToResponse(saved);
    }

    // GET BUSINESS ACCOUNT
    public BusinessAccResponse getBusinessAccount(
            String userEmail) {

        // Find logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // Find business account
        BusinessAcc businessAcc =
                businessAccRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Business account not found"
                                )
                        );

        return mapToResponse(businessAcc);
    }

    // MAP ENTITY TO RESPONSE
    private BusinessAccResponse mapToResponse(
            BusinessAcc businessAcc) {

        return BusinessAccResponse.builder()
                .id(businessAcc.getId())
                .userId(businessAcc.getUser().getId())
                .businessName(businessAcc.getBusinessName())
                .businessEmail(businessAcc.getBusinessEmail())
                .businessPhone(businessAcc.getBusinessPhone())
                .businessAddress(businessAcc.getBusinessAddress())
                .taxId(businessAcc.getTaxId())
                .createdAt(businessAcc.getCreatedAt())
                .updatedAt(businessAcc.getUpdatedAt())
                .build();
    }
    public BusinessAccResponse updateBusinessAccount(
            BusinessAccRequest request,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        BusinessAcc businessAcc =
                businessAccRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Business account not found"
                                )
                        );

        businessAcc.setBusinessName(request.getBusinessName());
        businessAcc.setBusinessEmail(request.getBusinessEmail());
        businessAcc.setBusinessPhone(request.getBusinessPhone());
        businessAcc.setBusinessAddress(request.getBusinessAddress());
        businessAcc.setTaxId(request.getTaxId());

        BusinessAcc updated =
                businessAccRepository.save(businessAcc);

        return mapToResponse(updated);
    }
}