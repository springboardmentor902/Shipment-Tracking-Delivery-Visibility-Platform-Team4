package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.ChangePasswordRequest;
import com.shiptrack.shiptrack_pro.dto.LoginRequest;
import com.shiptrack.shiptrack_pro.dto.LoginResponse;
import com.shiptrack.shiptrack_pro.dto.RegisterRequest;
import com.shiptrack.shiptrack_pro.dto.UserResponse;
import com.shiptrack.shiptrack_pro.dto.UserUpdateRequest;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse registerUser(RegisterRequest request) {
        log.info("📝 Registering user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("❌ Email already registered: {}", request.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered: " + request.getEmail()
            );
        }

        // Validate role
        Role requestedRole;
        try {
            requestedRole = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("❌ Invalid role: {}", request.getRole());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role: " + request.getRole() +
                            ". Must be one of: " + Arrays.toString(Role.values())
            );
        }

        // Prevent ADMINISTRATOR registration via public endpoint
        if (requestedRole == Role.ADMINISTRATOR) {
            log.warn("❌ Attempted to register ADMINISTRATOR via public endpoint");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administrator accounts cannot be created through registration."
            );
        }

        // Create user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(requestedRole.name())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);
        log.info("✅ User registered successfully: {}", savedUser.getEmail());
        log.info("✅ User role: {}", savedUser.getRole());

        return mapToResponse(savedUser);
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        log.info("🔐 Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("❌ User not found with email: {}", request.getEmail());
                    return new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid email or password"
                    );
                });

        log.info("✅ User found: {}, role: {}", user.getEmail(), user.getRole());

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("❌ Invalid password for user: {}", request.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Check account status
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("❌ Account is not active for user: {}, status: {}", request.getEmail(), user.getStatus());
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Account is not active. Current status: " + user.getStatus()
            );
        }

        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(updatedUser.getEmail(), updatedUser.getRole());
        log.info("✅ Login successful for user: {}, role: {}", updatedUser.getEmail(), updatedUser.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToResponse(updatedUser))
                .build();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("📋 Getting all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("📋 Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with id: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found with id: " + id
                    );
                });
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.info("📋 Getting user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with email: {}", email);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found with email: " + email
                    );
                });
        return mapToResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        log.info("📋 Getting current user: {}", email);
        return getUserByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("📝 Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with id: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found with id: " + id
                    );
                });

        // Check email uniqueness if it's being changed
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            log.warn("❌ Email already in use: {}", request.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already in use: " + request.getEmail()
            );
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);
        log.info("✅ User updated successfully: {}", updatedUser.getEmail());

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, String newRole) {
        log.info("📝 Updating role for user: {} to {}", id, newRole);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with id: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found with id: " + id
                    );
                });

        // Validate role
        Role role;
        try {
            role = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("❌ Invalid role: {}", newRole);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role: " + newRole +
                            ". Must be one of: " + Arrays.toString(Role.values())
            );
        }

        // Prevent multiple administrators
        if (role == Role.ADMINISTRATOR && userRepository.existsByRole("ADMINISTRATOR")) {
            log.warn("❌ Administrator already exists. Cannot create another.");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An administrator account already exists. Only one administrator is allowed."
            );
        }

        user.setRole(role.name());
        User updatedUser = userRepository.save(user);
        log.info("✅ Role updated successfully for user: {} to {}", updatedUser.getEmail(), updatedUser.getRole());

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        log.info("🔑 Changing password for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with email: {}", email);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found"
                    );
                });

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("❌ Current password is incorrect for user: {}", email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Current password is incorrect"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User updatedUser = userRepository.save(user);
        log.info("✅ Password changed successfully for user: {}", email);

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, String status, String reason) {
        log.info("📝 Updating user status: {} to {}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with id: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found with id: " + id
                    );
                });

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
        log.info("✅ User status updated: {} -> {}", id, status);

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        log.info("🔓 Activating user: {}", id);
        return updateUserStatus(id, "ACTIVE", "Activated by admin");
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        log.info("🔒 Deactivating user: {}", id);
        return updateUserStatus(id, "INACTIVE", "Deactivated by admin");
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("🗑️ Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with id: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found with id: " + id
                    );
                });

        if ("ADMINISTRATOR".equals(user.getRole()) &&
                userRepository.existsByRole("ADMINISTRATOR")) {
            log.warn("❌ Cannot delete the only administrator account");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot delete the only administrator account"
            );
        }

        userRepository.delete(user);
        log.info("✅ User deleted successfully: {}", id);
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