package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.BusinessAccountRequest;
import com.shiptrack.shiptrack_pro.dto.BusinessAccountResponse;
import com.shiptrack.shiptrack_pro.entity.BusinessAccount;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.BusinessAccountRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.BusinessAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAccountServiceImpl implements BusinessAccountService {

    private final BusinessAccountRepository businessAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BusinessAccountResponse createBusinessAccount(Long userId, BusinessAccountRequest request) {
        log.info("Creating business account for user: {}", userId);

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        // Check if user already has a business account
        if (businessAccountRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already has a business account"
            );
        }

        // Parse business type
        BusinessAccount.BusinessType businessType = null;
        if (request.getBusinessType() != null) {
            try {
                businessType = BusinessAccount.BusinessType.valueOf(
                        request.getBusinessType().toUpperCase()
                );
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid business type. Valid types: " +
                                java.util.Arrays.toString(BusinessAccount.BusinessType.values())
                );
            }
        }

        // Build business account
        BusinessAccount account = BusinessAccount.builder()
                .userId(userId)
                .companyName(request.getCompanyName())
                .gstNumber(request.getGstNumber())
                .businessAddress(request.getBusinessAddress())
                .industryType(request.getIndustryType())
                .businessPhone(request.getBusinessPhone())
                .businessEmail(request.getBusinessEmail())
                .website(request.getWebsite())
                .panNumber(request.getPanNumber())
                .registrationNumber(request.getRegistrationNumber())
                .taxId(request.getTaxId())
                .businessType(businessType)
                .isVerified(false)
                .notes(request.getNotes())
                .build();

        BusinessAccount savedAccount = businessAccountRepository.save(account);
        log.info("Business account created with ID: {}", savedAccount.getId());

        return BusinessAccountResponse.fromEntity(savedAccount);
    }

    @Override
    public BusinessAccountResponse getBusinessAccountByUserId(Long userId) {
        BusinessAccount account = businessAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business account not found for user: " + userId
                ));
        return BusinessAccountResponse.fromEntity(account);
    }

    @Override
    public BusinessAccountResponse getBusinessAccountById(Long id) {
        BusinessAccount account = businessAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business account not found with id: " + id
                ));
        return BusinessAccountResponse.fromEntity(account);
    }

    @Override
    @Transactional
    public BusinessAccountResponse updateBusinessAccount(Long userId, BusinessAccountRequest request) {
        log.info("Updating business account for user: {}", userId);

        BusinessAccount account = businessAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business account not found for user: " + userId
                ));

        // Update fields
        account.setCompanyName(request.getCompanyName());
        account.setGstNumber(request.getGstNumber());
        account.setBusinessAddress(request.getBusinessAddress());
        account.setIndustryType(request.getIndustryType());
        account.setBusinessPhone(request.getBusinessPhone());
        account.setBusinessEmail(request.getBusinessEmail());
        account.setWebsite(request.getWebsite());
        account.setPanNumber(request.getPanNumber());
        account.setRegistrationNumber(request.getRegistrationNumber());
        account.setTaxId(request.getTaxId());
        account.setNotes(request.getNotes());

        if (request.getBusinessType() != null) {
            try {
                account.setBusinessType(BusinessAccount.BusinessType.valueOf(
                        request.getBusinessType().toUpperCase()
                ));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid business type"
                );
            }
        }

        BusinessAccount updatedAccount = businessAccountRepository.save(account);
        log.info("Business account updated for user: {}", userId);

        return BusinessAccountResponse.fromEntity(updatedAccount);
    }

    @Override
    @Transactional
    public BusinessAccountResponse verifyBusinessAccount(Long accountId, Long adminId) {
        log.info("Verifying business account: {} by admin: {}", accountId, adminId);

        // Check if admin exists
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        // Verify admin role
        if (!"ADMINISTRATOR".equals(admin.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can verify business accounts"
            );
        }

        BusinessAccount account = businessAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business account not found with id: " + accountId
                ));

        if (Boolean.TRUE.equals(account.getIsVerified())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Business account is already verified"
            );
        }

        account.setIsVerified(true);
        account.setVerificationDate(LocalDateTime.now());
        account.setVerifiedBy(adminId);

        BusinessAccount verifiedAccount = businessAccountRepository.save(account);
        log.info("Business account verified: {}", accountId);

        return BusinessAccountResponse.fromEntity(verifiedAccount);
    }

    @Override
    public List<BusinessAccountResponse> getAllBusinessAccounts() {
        return businessAccountRepository.findAll()
                .stream()
                .map(BusinessAccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BusinessAccountResponse> getVerifiedBusinessAccounts() {
        return businessAccountRepository.findByIsVerified(true)
                .stream()
                .map(BusinessAccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BusinessAccountResponse> getUnverifiedBusinessAccounts() {
        return businessAccountRepository.findByIsVerified(false)
                .stream()
                .map(BusinessAccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBusinessAccount(Long accountId) {
        BusinessAccount account = businessAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Business account not found with id: " + accountId
                ));

        businessAccountRepository.delete(account);
        log.info("Business account deleted: {}", accountId);
    }

    @Override
    public boolean hasBusinessAccount(Long userId) {
        return businessAccountRepository.existsByUserId(userId);
    }
}