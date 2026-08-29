package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.ChangePasswordRequest;
import com.shiptrack.shiptrack_pro.dto.UserResponse;
import com.shiptrack.shiptrack_pro.dto.UserStatusUpdateRequest;
import com.shiptrack.shiptrack_pro.dto.UserUpdateRequest;
import com.shiptrack.shiptrack_pro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getCurrentUser(email));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequest request) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(userService.updateUser(user.getId(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<UserResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.changePassword(email, request));
    }

    /**
     * ✅ NEW: Update user status (Operator or Admin)
     * PATCH /api/users/{id}/status
     * Roles: LOGISTICS_OPERATOR, ADMINISTRATOR
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        UserResponse currentUser = userService.getUserByEmail(email);

        // Only ADMINISTRATOR can update ADMINISTRATOR status
        UserResponse targetUser = userService.getUserById(id);
        if ("ADMINISTRATOR".equals(targetUser.getRole()) &&
                !"ADMINISTRATOR".equals(currentUser.getRole())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.updateUserStatus(id, request.getStatus(), request.getReason()));
    }
}