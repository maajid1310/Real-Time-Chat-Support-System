package com.chatbot.chatbot.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.chatbot.chatbot.dto.request.UserManagementRequest;
import com.chatbot.chatbot.dto.response.ApiResponse;
import com.chatbot.chatbot.dto.response.UserManagementResponse;
import com.chatbot.chatbot.service.UserManagementService;

import jakarta.validation.Valid;

/**
 * AdminController
 * 
 * REST Controller for ADMIN operations
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserManagementService userManagementService;

    public AdminController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * Get all users
     * 
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserManagementResponse> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    /**
     * Get user by ID
     * 
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        UserManagementResponse user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", user)
        );
    }

    /**
     * Create new user
     * 
     * POST /api/admin/users
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserManagementRequest request) {
        UserManagementResponse user = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("User created successfully", user)
        );
    }

    /**
     * Update existing user
     * 
     * PUT /api/admin/users/{userId}
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserManagementRequest request) {
        UserManagementResponse user = userManagementService.updateUser(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", user)
        );
    }

    /**
     * Delete user (soft delete)
     * 
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully")
        );
    }

    /**
     * Toggle user active status
     * 
     * PATCH /api/admin/users/{userId}/toggle-status
     */
    @PatchMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse> toggleUserStatus(@PathVariable Long userId) {
        UserManagementResponse user = userManagementService.toggleUserStatus(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User status updated successfully", user)
        );
    }

    /**
     * Get users by role
     * 
     * GET /api/admin/users/role/{roleName}
     */
    @GetMapping("/users/role/{roleName}")
    public ResponseEntity<ApiResponse> getUsersByRole(@PathVariable String roleName) {
        List<UserManagementResponse> users = userManagementService.getUsersByRole(roleName);
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    /**
     * Get user statistics
     * 
     * GET /api/admin/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse> getUserStatistics() {
        Object stats = userManagementService.getUserStatistics();
        return ResponseEntity.ok(
                ApiResponse.success("Statistics retrieved successfully", stats)
        );
    }
}
