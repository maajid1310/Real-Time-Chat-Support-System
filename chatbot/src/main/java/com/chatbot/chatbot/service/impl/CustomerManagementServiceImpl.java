package com.chatbot.chatbot.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.chatbot.dto.response.ChatSessionDetailResponse;
import com.chatbot.chatbot.entity.ChatSession;
import com.chatbot.chatbot.entity.User;
import com.chatbot.chatbot.enums.ChatStatus;
import com.chatbot.chatbot.exception.ForbiddenException;
import com.chatbot.chatbot.exception.ResourceNotFoundException;
import com.chatbot.chatbot.repository.ChatSessionRepository;
import com.chatbot.chatbot.repository.UserRepository;
import com.chatbot.chatbot.service.CustomerManagementService;

/**
 * CustomerManagementServiceImpl
 * 
 * Implementation of customer operations
 */
@Service
@Transactional
public class CustomerManagementServiceImpl implements CustomerManagementService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    public CustomerManagementServiceImpl(ChatSessionRepository chatSessionRepository,
                                         UserRepository userRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSessionDetailResponse> getCustomerChatSessions(Long customerId) {
        List<ChatSession> sessions = chatSessionRepository.findByCustomerId(customerId);
        return sessions.stream()
                .map(this::mapToChatSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChatSessionDetailResponse getCustomerChatSession(Long customerId, Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with ID: " + sessionId));

        // Verify customer owns this session
        if (!session.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("You are not authorized to view this chat session");
        }

        return mapToChatSessionResponse(session);
    }

    @Override
    public ChatSessionDetailResponse createChatSession(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        ChatSession session = new ChatSession();
        session.setCustomer(customer);
        session.setStatus(ChatStatus.WAITING);

        ChatSession savedSession = chatSessionRepository.save(session);
        return mapToChatSessionResponse(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getCustomerProfile(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", customer.getId());
        profile.put("firstName", customer.getFirstName());
        profile.put("lastName", customer.getLastName());
        profile.put("email", customer.getEmail());
        profile.put("phone", customer.getPhone());
        profile.put("active", customer.isActive());
        profile.put("createdAt", customer.getCreatedAt());

        // Add chat statistics
        List<ChatSession> sessions = chatSessionRepository.findByCustomerId(customerId);
        profile.put("totalChats", sessions.size());
        profile.put("activeChats", sessions.stream()
                .filter(s -> s.getStatus() == ChatStatus.ACTIVE)
                .count());

        return profile;
    }

    /**
     * Helper method to map ChatSession to Response DTO
     */
    private ChatSessionDetailResponse mapToChatSessionResponse(ChatSession session) {
        ChatSessionDetailResponse response = new ChatSessionDetailResponse();
        response.setId(session.getId());
        response.setStatus(session.getStatus().name());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());

        // Customer details
        User customer = session.getCustomer();
        response.setCustomerId(customer.getId());
        response.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        response.setCustomerEmail(customer.getEmail());

        // Agent details (if assigned)
        if (session.getAgent() != null) {
            User agent = session.getAgent();
            response.setAgentId(agent.getId());
            response.setAgentName(agent.getFirstName() + " " + agent.getLastName());
            response.setAgentEmail(agent.getEmail());
        }

        // Message count
        response.setMessageCount(session.getMessages().size());

        return response;
    }
}
