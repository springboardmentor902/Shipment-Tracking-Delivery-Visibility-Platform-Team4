package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.BusinessAccountRequest;
import com.shiptrack.shiptrack_pro.dto.BusinessAccountResponse;

import java.util.List;

public interface BusinessAccountService {

    // Create business account for a user
    BusinessAccountResponse createBusinessAccount(Long userId, BusinessAccountRequest request);

    // Get business account by user ID
    BusinessAccountResponse getBusinessAccountByUserId(Long userId);

    // Get business account by ID
    BusinessAccountResponse getBusinessAccountById(Long id);

    // Update business account
    BusinessAccountResponse updateBusinessAccount(Long userId, BusinessAccountRequest request);

    // Verify business account (Admin only)
    BusinessAccountResponse verifyBusinessAccount(Long accountId, Long adminId);

    // Get all business accounts (Admin only)
    List<BusinessAccountResponse> getAllBusinessAccounts();

    // Get verified business accounts
    List<BusinessAccountResponse> getVerifiedBusinessAccounts();

    // Get unverified business accounts (Admin only)
    List<BusinessAccountResponse> getUnverifiedBusinessAccounts();

    // Delete business account
    void deleteBusinessAccount(Long accountId);

    // Check if user has business account
    boolean hasBusinessAccount(Long userId);
}