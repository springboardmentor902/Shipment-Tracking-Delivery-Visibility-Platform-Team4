package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.dto.PackageResponse;
import com.shiptrack.shiptrack_pro.service.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    /**
     * GET /api/packages/shipment/{shipmentId} - Get all packages for a shipment
     */
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<List<PackageResponse>> getPackagesByShipment(
            @PathVariable Long shipmentId) {
        log.info("Getting packages for shipment: {}", shipmentId);
        return ResponseEntity.ok(packageService.getPackagesByShipment(shipmentId));
    }

    /**
     * GET /api/packages/{id} - Get package by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> getPackageById(@PathVariable Long id) {
        log.info("Getting package by id: {}", id);
        return ResponseEntity.ok(packageService.getPackageById(id));
    }

    /**
     * POST /api/packages - Create a new package (for a shipment)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<PackageResponse> createPackage(
            @RequestParam Long shipmentId,
            @Valid @RequestBody PackageRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Creating package for shipment: {} by user: {}", shipmentId, userId);
        PackageResponse response = packageService.createPackage(shipmentId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/packages/{id} - Update package
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<PackageResponse> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating package: {} by user: {}", id, userId);
        return ResponseEntity.ok(packageService.updatePackage(id, request));
    }

    /**
     * DELETE /api/packages/{id} - Delete package
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<Void> deletePackage(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Deleting package: {} by user: {}", id, userId);
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/packages/shipment/{shipmentId} - Delete all packages for a shipment
     */
    @DeleteMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<Void> deletePackagesByShipment(
            @PathVariable Long shipmentId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Deleting all packages for shipment: {} by user: {}", shipmentId, userId);
        packageService.deletePackagesByShipment(shipmentId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return 1L;
        }
        return 1L;
    }
}