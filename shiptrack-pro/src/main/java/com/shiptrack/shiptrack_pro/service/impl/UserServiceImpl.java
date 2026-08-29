package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.*;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.security.JwtUtil;
import com.shiptrack.shiptrack_pro.security.Role;
import com.shiptrack.shiptrack_pro.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j  // ✅ ADD THIS for logging
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered: " + request.getEmail()
            );
        }

        Role requestedRole;
        try {
            requestedRole = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role: " + request.getRole() +
                            ". Must be one of: " + Arrays.toString(Role.values())
            );
        }

        if (requestedRole == Role.ADMINISTRATOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administrator accounts cannot be created through registration."
            );
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(requestedRole.name())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Account is not active. Current status: " + user.getStatus()
            );
        }

        user.setLastLoginAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(updatedUser.getEmail(), updatedUser.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToResponse(updatedUser))
                .build();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with email: " + email
                ));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        return getUserByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already in use: " + request.getEmail()
            );
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));

        Role role;
        try {
            role = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role: " + newRole +
                            ". Must be one of: " + Arrays.toString(Role.values())
            );
        }

        if (role == Role.ADMINISTRATOR && userRepository.existsByRole("ADMINISTRATOR")) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An administrator account already exists. Only one administrator is allowed."
            );
        }

        user.setRole(role.name());
        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Current password is incorrect"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    // ✅ NEW: Update user status
    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, String status, String reason) {
        log.info("Updating user status: {} to {}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));

        // Validate status
        String validStatus = status.toUpperCase();
        if (!Arrays.asList("ACTIVE", "INACTIVE", "SUSPENDED").contains(validStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status. Must be ACTIVE, INACTIVE, or SUSPENDED"
            );
        }

        // Prevent deactivating the only ADMINISTRATOR
        if ("ADMINISTRATOR".equals(user.getRole()) && !"ACTIVE".equals(validStatus)) {
            // Check if this is the only admin
            if (userRepository.existsByRole("ADMINISTRATOR") &&
                    userRepository.findByRole("ADMINISTRATOR").size() == 1) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Cannot deactivate the only administrator account"
                );
            }
        }

        String oldStatus = user.getStatus();
        user.setStatus(validStatus);

        // Add note about status change
        String notes = user.getNotes();
        if (notes == null) notes = "";
        notes += "\nStatus changed from " + oldStatus + " to " + validStatus +
                " on " + LocalDateTime.now();
        if (reason != null && !reason.isEmpty()) {
            notes += " Reason: " + reason;
        }
        user.setNotes(notes);

        User updatedUser = userRepository.save(user);
        log.info("User status updated: {} -> {}", id, status);

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        return updateUserStatus(id, "ACTIVE", "Activated by admin");
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        return updateUserStatus(id, "INACTIVE", "Deactivated by admin");
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));

        if ("ADMINISTRATOR".equals(user.getRole()) &&
                userRepository.existsByRole("ADMINISTRATOR")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot delete the only administrator account"
            );
        }

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}