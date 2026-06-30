package com.chatbot.chatbot.controller.customer;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.chatbot.chatbot.dto.response.ApiResponse;
import com.chatbot.chatbot.dto.response.ChatSessionDetailResponse;
import com.chatbot.chatbot.security.service.CustomUserDetails;
import com.chatbot.chatbot.service.CustomerManagementService;

/**
 * CustomerController
 * 
 * REST Controller for CUSTOMER operations
 * All endpoints require CUSTOMER role
 */
@RestController
@RequestMapping("/api/customer")
@Validated
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final CustomerManagementService customerManagementService;

    public CustomerController(CustomerManagementService customerManagementService) {
        this.customerManagementService = customerManagementService;
    }

    /**
     * Get customer profile
     * 
     * GET /api/customer/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Object profile = customerManagementService.getCustomerProfile(userDetails.getUser().getId());
        
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", profile)
        );
    }

    /**
     * Get customer's chat sessions
     * 
     * GET /api/customer/chats
     */
    @GetMapping("/chats")
    public ResponseEntity<ApiResponse> getCustomerChats(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<ChatSessionDetailResponse> sessions = customerManagementService.getCustomerChatSessions(
                userDetails.getUser().getId());
        
        return ResponseEntity.ok(
                ApiResponse.success("Chat sessions retrieved successfully", sessions)
        );
    }

    /**
     * Get specific chat session
     * 
     * GET /api/customer/chats/{sessionId}
     */
    @GetMapping("/chats/{sessionId}")
    public ResponseEntity<ApiResponse> getChatSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ChatSessionDetailResponse session = customerManagementService.getCustomerChatSession(
                userDetails.getUser().getId(), sessionId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Chat session retrieved successfully", session)
        );
    }

    /**
     * Create new chat session
     * 
     * POST /api/customer/chats
     */
    @PostMapping("/chats")
    public ResponseEntity<ApiResponse> createChatSession(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ChatSessionDetailResponse session = customerManagementService.createChatSession(
                userDetails.getUser().getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Chat session created successfully", session)
        );
    }
}
