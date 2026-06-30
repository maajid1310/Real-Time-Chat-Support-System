package com.chatbot.chatbot.service;

import java.util.List;

import com.chatbot.chatbot.dto.request.UserManagementRequest;
import com.chatbot.chatbot.dto.response.UserManagementResponse;

/**
 * UserManagementService
 * 
 * Service interface for ADMIN user management operations
 */
public interface UserManagementService {

    /**
     * Get all users (ADMIN only)
     */
    List<UserManagementResponse> getAllUsers();

    /**
     * Get user by ID (ADMIN only)
     */
    UserManagementResponse getUserById(Long userId);

    /**
     * Create new user (ADMIN only)
     */
    UserManagementResponse createUser(UserManagementRequest request);

    /**
     * Update existing user (ADMIN only)
     */
    UserManagementResponse updateUser(Long userId, UserManagementRequest request);

    /**
     * Delete user (ADMIN only)
     */
    void deleteUser(Long userId);

    /**
     * Activate/Deactivate user (ADMIN only)
     */
    UserManagementResponse toggleUserStatus(Long userId);

    /**
     * Get users by role (ADMIN only)
     */
    List<UserManagementResponse> getUsersByRole(String roleName);

    /**
     * Get user statistics (ADMIN only)
     */
    Object getUserStatistics();
}
