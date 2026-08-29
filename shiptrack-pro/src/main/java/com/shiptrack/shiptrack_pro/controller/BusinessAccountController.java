package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.BusinessAccountRequest;
import com.shiptrack.shiptrack_pro.dto.BusinessAccountResponse;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.BusinessAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessAccountController {

    private final BusinessAccountService businessAccountService;
    private final UserRepository userRepository;  // ✅ ADD THIS

    /**
     * Create business account (for authenticated user)
     * POST /api/business/account
     */
    @PostMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessAccountResponse> createBusinessAccount(
            Authentication authentication,
            @Valid @RequestBody BusinessAccountRequest request) {

        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Creating business account for user: {}", userId);

        BusinessAccountResponse response = businessAccountService.createBusinessAccount(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get my business account (for authenticated user)
     * GET /api/business/account/me
     */
    @GetMapping("/account/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessAccountResponse> getMyBusinessAccount(
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Getting business account for user: {}", userId);

        BusinessAccountResponse response = businessAccountService.getBusinessAccountByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update my business account (for authenticated user)
     * PUT /api/business/account/me
     */
    @PutMapping("/account/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessAccountResponse> updateMyBusinessAccount(
            Authentication authentication,
            @Valid @RequestBody BusinessAccountRequest request) {

        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Updating business account for user: {}", userId);

        BusinessAccountResponse response = businessAccountService.updateBusinessAccount(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get business account by user ID (Admin only)
     * GET /api/business/account/user/{userId}
     */
    @GetMapping("/account/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<BusinessAccountResponse> getBusinessAccountByUserId(
            @PathVariable Long userId) {

        log.info("Getting business account for user: {}", userId);
        BusinessAccountResponse response = businessAccountService.getBusinessAccountByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all business accounts (Admin only)
     * GET /api/business/accounts
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<BusinessAccountResponse>> getAllBusinessAccounts() {
        log.info("Getting all business accounts");
        return ResponseEntity.ok(businessAccountService.getAllBusinessAccounts());
    }

    /**
     * Get verified business accounts (Admin only)
     * GET /api/business/accounts/verified
     */
    @GetMapping("/accounts/verified")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<BusinessAccountResponse>> getVerifiedBusinessAccounts() {
        log.info("Getting verified business accounts");
        return ResponseEntity.ok(businessAccountService.getVerifiedBusinessAccounts());
    }

    /**
     * Get unverified business accounts (Admin only)
     * GET /api/business/accounts/unverified
     */
    @GetMapping("/accounts/unverified")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<BusinessAccountResponse>> getUnverifiedBusinessAccounts() {
        log.info("Getting unverified business accounts");
        return ResponseEntity.ok(businessAccountService.getUnverifiedBusinessAccounts());
    }

    /**
     * Verify business account (Admin only)
     * PATCH /api/business/account/{accountId}/verify
     */
    @PatchMapping("/account/{accountId}/verify")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<BusinessAccountResponse> verifyBusinessAccount(
            @PathVariable Long accountId,
            Authentication authentication) {

        Long adminId = getUserIdFromAuthentication(authentication);
        log.info("Verifying business account: {} by admin: {}", accountId, adminId);

        BusinessAccountResponse response = businessAccountService.verifyBusinessAccount(accountId, adminId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete business account (Admin only)
     * DELETE /api/business/account/{accountId}
     */
    @DeleteMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteBusinessAccount(@PathVariable Long accountId) {
        log.info("Deleting business account: {}", accountId);
        businessAccountService.deleteBusinessAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if user has business account
     * GET /api/business/account/check
     */
    @GetMapping("/account/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> hasBusinessAccount(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(businessAccountService.hasBusinessAccount(userId));
    }

    // ✅ FIXED: Extract user ID from authentication
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        log.info("Extracting user ID for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with email: " + email
                ));

        return user.getId();
    }
}