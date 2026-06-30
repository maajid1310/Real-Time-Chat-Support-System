package com.chatbot.chatbot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.chatbot.dto.request.AgentAvailabilityRequest;
import com.chatbot.chatbot.dto.response.AgentAvailabilityResponse;
import com.chatbot.chatbot.dto.response.ChatSessionDetailResponse;
import com.chatbot.chatbot.entity.AgentAvailability;
import com.chatbot.chatbot.entity.ChatSession;
import com.chatbot.chatbot.entity.User;
import com.chatbot.chatbot.enums.AgentStatus;
import com.chatbot.chatbot.enums.ChatStatus;
import com.chatbot.chatbot.exception.BadRequestException;
import com.chatbot.chatbot.exception.ForbiddenException;
import com.chatbot.chatbot.exception.ResourceNotFoundException;
import com.chatbot.chatbot.repository.AgentAvailabilityRepository;
import com.chatbot.chatbot.repository.ChatSessionRepository;
import com.chatbot.chatbot.repository.UserRepository;
import com.chatbot.chatbot.service.AgentManagementService;

/**
 * AgentManagementServiceImpl
 * 
 * Implementation of agent operations
 */
@Service
@Transactional
public class AgentManagementServiceImpl implements AgentManagementService {

    private final AgentAvailabilityRepository availabilityRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    public AgentManagementServiceImpl(AgentAvailabilityRepository availabilityRepository,
                                      ChatSessionRepository chatSessionRepository,
                                      UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AgentAvailabilityResponse updateAvailability(Long agentId, AgentAvailabilityRequest request) {
        // Validate agent exists and is an AGENT
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with ID: " + agentId));

        if (!"AGENT".equals(agent.getRole().getRoleName())) {
            throw new BadRequestException("User is not an agent");
        }

        // Create or update availability
        AgentAvailability availability = availabilityRepository.findLatestByAgentId(agentId)
                .orElse(new AgentAvailability());

        availability.setAgent(agent);
        availability.setStatus(AgentStatus.valueOf(request.getStatus()));

        AgentAvailability savedAvailability = availabilityRepository.save(availability);
        return mapToAvailabilityResponse(savedAvailability);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentAvailabilityResponse getAgentAvailability(Long agentId) {
        AgentAvailability availability = availabilityRepository.findLatestByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("No availability record found for agent: " + agentId));

        return mapToAvailabilityResponse(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSessionDetailResponse> getAgentChatSessions(Long agentId) {
        List<ChatSession> sessions = chatSessionRepository.findByAgentId(agentId);
        return sessions.stream()
                .map(this::mapToChatSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSessionDetailResponse> getAgentActiveChats(Long agentId) {
        List<ChatSession> activeSessions = chatSessionRepository.findActiveChatsByAgentId(agentId);
        return activeSessions.stream()
                .map(this::mapToChatSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChatSessionDetailResponse acceptChatSession(Long agentId, Long sessionId) {
        // Validate agent
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with ID: " + agentId));

        if (!"AGENT".equals(agent.getRole().getRoleName())) {
            throw new BadRequestException("User is not an agent");
        }

        // Find chat session
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with ID: " + sessionId));

        // Check if session is waiting
        if (session.getStatus() != ChatStatus.WAITING) {
            throw new BadRequestException("Chat session is not in WAITING status");
        }

        // Assign agent and change status
        session.setAgent(agent);
        session.setStatus(ChatStatus.ACTIVE);

        ChatSession updatedSession = chatSessionRepository.save(session);
        return mapToChatSessionResponse(updatedSession);
    }

    @Override
    public ChatSessionDetailResponse endChatSession(Long agentId, Long sessionId) {
        // Find chat session
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with ID: " + sessionId));

        // Verify agent owns this session
        if (session.getAgent() == null || !session.getAgent().getId().equals(agentId)) {
            throw new ForbiddenException("You are not authorized to end this chat session");
        }

        // Check if session is active
        if (session.getStatus() != ChatStatus.ACTIVE) {
            throw new BadRequestException("Chat session is not in ACTIVE status");
        }

        // End session
        session.setStatus(ChatStatus.CLOSED);
        session.setEndTime(LocalDateTime.now());

        ChatSession updatedSession = chatSessionRepository.save(session);
        return mapToChatSessionResponse(updatedSession);
    }

    /**
     * Helper method to map AgentAvailability to Response DTO
     */
    private AgentAvailabilityResponse mapToAvailabilityResponse(AgentAvailability availability) {
        User agent = availability.getAgent();
        String agentName = agent.getFirstName() + " " + agent.getLastName();

        return new AgentAvailabilityResponse(
                availability.getId(),
                agent.getId(),
                agentName,
                availability.getStatus().name(),
                availability.getLastUpdated()
        );
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
