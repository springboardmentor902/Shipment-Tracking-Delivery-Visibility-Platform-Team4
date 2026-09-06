package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.AdminAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.BusinessAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.CustomerAnalyticsResponse;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;


    // =====================================================
    // CUSTOMER
    // =====================================================

    @GetMapping("/customer")
    public ResponseEntity<CustomerAnalyticsResponse>
    getCustomerAnalytics(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        if (!"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                analyticsService.getCustomerAnalytics(
                        user.getId())
        );
    }


    // =====================================================
    // BUSINESS CLIENT
    // =====================================================

    @GetMapping("/business")
    public ResponseEntity<BusinessAnalyticsResponse>
    getBusinessAnalytics(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        if (!"BUSINESS_CLIENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                analyticsService.getBusinessAnalytics(
                        user.getId())
        );
    }


    // =====================================================
    // ADMINISTRATOR
    // =====================================================

    @GetMapping("/admin")
    public ResponseEntity<AdminAnalyticsResponse>
    getAdminAnalytics(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        if (!"ADMINISTRATOR".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                analyticsService.getAdminAnalytics()
        );
    }


    private User getAuthenticatedUser(
            Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        ));
    }
}