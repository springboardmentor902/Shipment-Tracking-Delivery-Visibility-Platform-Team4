package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.entity.Package;
import com.shiptrack.shiptrack_pro.service.PackageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "http://localhost:5173")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @PostMapping
    public ResponseEntity<Package> createPackage(
            @RequestBody PackageRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        Package createdPackage =
                packageService.createPackage(
                        request,
                        userEmail
                );

        return new ResponseEntity<>(
                createdPackage,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<Package>> getAllPackages(
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                packageService.getAllPackages(userEmail)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Package> getPackageById(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                packageService.getPackageById(
                        id,
                        userEmail
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Package> updatePackage(
            @PathVariable Long id,
            @RequestBody PackageRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                packageService.updatePackage(
                        id,
                        request,
                        userEmail
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();

        packageService.deletePackage(
                id,
                userEmail
        );

        return ResponseEntity.noContent().build();
    }
}