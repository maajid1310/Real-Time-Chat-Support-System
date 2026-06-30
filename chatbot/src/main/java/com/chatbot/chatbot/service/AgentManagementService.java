package com.chatbot.chatbot.service;

import java.util.List;

import com.chatbot.chatbot.dto.request.AgentAvailabilityRequest;
import com.chatbot.chatbot.dto.response.AgentAvailabilityResponse;
import com.chatbot.chatbot.dto.response.ChatSessionDetailResponse;

/**
 * AgentManagementService
 * 
 * Service interface for AGENT operations
 */
public interface AgentManagementService {

    /**
     * Update agent availability status
     */
    AgentAvailabilityResponse updateAvailability(Long agentId, AgentAvailabilityRequest request);

    /**
     * Get agent's current availability
     */
    AgentAvailabilityResponse getAgentAvailability(Long agentId);

    /**
     * Get all chat sessions assigned to agent
     */
    List<ChatSessionDetailResponse> getAgentChatSessions(Long agentId);

    /**
     * Get active chats for agent
     */
    List<ChatSessionDetailResponse> getAgentActiveChats(Long agentId);

    /**
     * Accept a waiting chat session
     */
    ChatSessionDetailResponse acceptChatSession(Long agentId, Long sessionId);

    /**
     * End a chat session
     */
    ChatSessionDetailResponse endChatSession(Long agentId, Long sessionId);
}
