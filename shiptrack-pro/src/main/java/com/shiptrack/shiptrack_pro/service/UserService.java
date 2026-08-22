package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.*;

import java.util.List;

public interface UserService {

    // Authentication
    UserResponse registerUser(RegisterRequest request);
    LoginResponse loginUser(LoginRequest request);

    // User Management
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse getCurrentUser(String email);

    // User Updates
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse updateUserRole(Long id, String newRole);
    UserResponse changePassword(String email, ChangePasswordRequest request);

    // ✅ NEW: Update user status (for operator or admin)
    UserResponse updateUserStatus(Long id, String status, String reason);

    // User Management Actions
    UserResponse activateUser(Long id);
    UserResponse deactivateUser(Long id);
    void deleteUser(Long id);
}