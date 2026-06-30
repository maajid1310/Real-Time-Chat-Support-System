package com.chatbot.chatbot.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.chatbot.dto.request.UserManagementRequest;
import com.chatbot.chatbot.dto.response.UserManagementResponse;
import com.chatbot.chatbot.entity.Role;
import com.chatbot.chatbot.entity.User;
import com.chatbot.chatbot.exception.BadRequestException;
import com.chatbot.chatbot.exception.ResourceNotFoundException;
import com.chatbot.chatbot.repository.RoleRepository;
import com.chatbot.chatbot.repository.UserRepository;
import com.chatbot.chatbot.service.UserManagementService;

/**
 * UserManagementServiceImpl
 * 
 * Implementation of admin user management operations
 */
@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementServiceImpl(UserRepository userRepository, 
                                     RoleRepository roleRepository,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserManagementResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToResponse(user);
    }

    @Override
    public UserManagementResponse createUser(UserManagementRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        // Find role
        Role role = roleRepository.findByRoleName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        // Create user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setActive(request.getActive() != null ? request.getActive() : true);
        
        // Set default password (should be changed on first login)
        String defaultPassword = "Welcome@123";
        user.setPassword(passwordEncoder.encode(defaultPassword));

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public UserManagementResponse updateUser(Long userId, UserManagementRequest request) {
        // Find existing user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        // Find role
        Role role = roleRepository.findByRoleName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        // Update user
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setActive(request.getActive() != null ? request.getActive() : user.isActive());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Soft delete by deactivating
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public UserManagementResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setActive(!user.isActive());
        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getUsersByRole(String roleName) {
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Object getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", userRepository.count());
        stats.put("totalAdmins", userRepository.countByRoleName("ADMIN"));
        stats.put("totalAgents", userRepository.countByRoleName("AGENT"));
        stats.put("totalCustomers", userRepository.countByRoleName("CUSTOMER"));
        
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
        stats.put("activeUsers", activeUsers.size());
        
        return stats;
    }

    /**
     * Helper method to map User entity to UserManagementResponse DTO
     */
    private UserManagementResponse mapToResponse(User user) {
        return new UserManagementResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getRoleName(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
