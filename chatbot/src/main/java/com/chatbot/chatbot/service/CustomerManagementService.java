package com.chatbot.chatbot.service;

import java.util.List;

import com.chatbot.chatbot.dto.response.ChatSessionDetailResponse;

/**
 * CustomerManagementService
 * 
 * Service interface for CUSTOMER operations
 */
public interface CustomerManagementService {

    /**
     * Get customer's own chat sessions
     */
    List<ChatSessionDetailResponse> getCustomerChatSessions(Long customerId);

    /**
     * Get specific chat session (only if owned by customer)
     */
    ChatSessionDetailResponse getCustomerChatSession(Long customerId, Long sessionId);

    /**
     * Create new chat session for customer
     */
    ChatSessionDetailResponse createChatSession(Long customerId);

    /**
     * Get customer profile
     */
    Object getCustomerProfile(Long customerId);
}
