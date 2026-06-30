package com.chatbot.chatbot.controller.agent;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.chatbot.chatbot.dto.request.AgentAvailabilityRequest;
import com.chatbot.chatbot.dto.response.AgentAvailabilityResponse;
import com.chatbot.chatbot.dto.response.ApiResponse;
import com.chatbot.chatbot.dto.response.ChatSessionDetailResponse;
import com.chatbot.chatbot.security.service.CustomUserDetails;
import com.chatbot.chatbot.service.AgentManagementService;

import jakarta.validation.Valid;

/**
 * AgentController
 * 
 * REST Controller for AGENT operations
 * All endpoints require AGENT role
 */
@RestController
@RequestMapping("/api/agent")
@Validated
@PreAuthorize("hasRole('AGENT')")
public class AgentController {

    private final AgentManagementService agentManagementService;

    public AgentController(AgentManagementService agentManagementService) {
        this.agentManagementService = agentManagementService;
    }

    /**
     * Update agent availability status
     * 
     * PUT /api/agent/availability
     */
    @PutMapping("/availability")
    public ResponseEntity<ApiResponse> updateAvailability(
            @Valid @RequestBody AgentAvailabilityRequest request,
            Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        AgentAvailabilityResponse availability = agentManagementService.updateAvailability(
                userDetails.getUser().getId(), request);
        
        return ResponseEntity.ok(
                ApiResponse.success("Availability updated successfully", availability)
        );
    }

    /**
     * Get agent's current availability
     * 
     * GET /api/agent/availability
     */
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse> getAvailability(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        AgentAvailabilityResponse availability = agentManagementService.getAgentAvailability(
                userDetails.getUser().getId());
        
        return ResponseEntity.ok(
                ApiResponse.success("Availability retrieved successfully", availability)
        );
    }

    /**
     * Get all chat sessions assigned to agent
     * 
     * GET /api/agent/chats
     */
    @GetMapping("/chats")
    public ResponseEntity<ApiResponse> getAgentChats(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<ChatSessionDetailResponse> sessions = agentManagementService.getAgentChatSessions(
                userDetails.getUser().getId());
        
        return ResponseEntity.ok(
                ApiResponse.success("Chat sessions retrieved successfully", sessions)
        );
    }

    /**
     * Get active chats for agent
     * 
     * GET /api/agent/chats/active
     */
    @GetMapping("/chats/active")
    public ResponseEntity<ApiResponse> getActiveChats(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<ChatSessionDetailResponse> sessions = agentManagementService.getAgentActiveChats(
                userDetails.getUser().getId());
        
        return ResponseEntity.ok(
                ApiResponse.success("Active chats retrieved successfully", sessions)
        );
    }

    /**
     * Accept a waiting chat session
     * 
     * POST /api/agent/chats/{sessionId}/accept
     */
    @PostMapping("/chats/{sessionId}/accept")
    public ResponseEntity<ApiResponse> acceptChatSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ChatSessionDetailResponse session = agentManagementService.acceptChatSession(
                userDetails.getUser().getId(), sessionId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Chat session accepted successfully", session)
        );
    }

    /**
     * End a chat session
     * 
     * POST /api/agent/chats/{sessionId}/end
     */
    @PostMapping("/chats/{sessionId}/end")
    public ResponseEntity<ApiResponse> endChatSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ChatSessionDetailResponse session = agentManagementService.endChatSession(
                userDetails.getUser().getId(), sessionId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Chat session ended successfully", session)
        );
    }
}
