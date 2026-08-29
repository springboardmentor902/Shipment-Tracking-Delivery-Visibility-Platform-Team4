package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.BusinessAccRequest;
import com.shiptrack.shiptrack_pro.dto.BusinessAccResponse;
import com.shiptrack.shiptrack_pro.service.BusinessAccService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business_acc")
@RequiredArgsConstructor
public class BusinessAccController {

    private final BusinessAccService businessAccService;

    // CREATE BUSINESS ACCOUNT
    @PostMapping
    public ResponseEntity<BusinessAccResponse> createBusinessAccount(
            @Valid @RequestBody BusinessAccRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        BusinessAccResponse response =
                businessAccService.createBusinessAccount(
                        request,
                        userEmail
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<BusinessAccResponse> getBusinessAccount(
            Authentication authentication) {

        String userEmail = authentication.getName();

        BusinessAccResponse response =
                businessAccService.getBusinessAccount(userEmail);

        return ResponseEntity.ok(response);
    }
    @PutMapping
    public ResponseEntity<BusinessAccResponse> updateBusinessAccount(
            @Valid @RequestBody BusinessAccRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        BusinessAccResponse response =
                businessAccService.updateBusinessAccount(
                        request,
                        userEmail
                );

        return ResponseEntity.ok(response);
    }
}