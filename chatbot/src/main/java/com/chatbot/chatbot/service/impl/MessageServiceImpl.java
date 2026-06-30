package com.chatbot.chatbot.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.chatbot.dto.request.MessageStatusRequest;
import com.chatbot.chatbot.dto.request.SendMessageRequest;
import com.chatbot.chatbot.dto.request.TypingRequest;
import com.chatbot.chatbot.dto.response.ChatMessageResponse;
import com.chatbot.chatbot.dto.response.MessageStatusResponse;
import com.chatbot.chatbot.dto.response.TypingResponse;
import com.chatbot.chatbot.entity.ChatSession;
import com.chatbot.chatbot.entity.Message;
import com.chatbot.chatbot.entity.User;
import com.chatbot.chatbot.enums.MessageStatus;
import com.chatbot.chatbot.exception.BadRequestException;
import com.chatbot.chatbot.exception.ResourceNotFoundException;
import com.chatbot.chatbot.mapper.EntityMapper;
import com.chatbot.chatbot.repository.ChatSessionRepository;
import com.chatbot.chatbot.repository.MessageRepository;
import com.chatbot.chatbot.repository.UserRepository;
import com.chatbot.chatbot.service.MessageService;
import com.chatbot.chatbot.websocket.ChatMessageBroker;

/**
 * Message Service Implementation
 *
 * Handles all core messaging operations:
 *  - Persist message to MySQL
 *  - Broadcast via WebSocket
 *  - Update message status (SENT → DELIVERED → READ)
 *  - Broadcast typing indicator
 *  - Paginated history retrieval
 */
@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageBroker chatMessageBroker;
    private final EntityMapper entityMapper;

    public MessageServiceImpl(MessageRepository messageRepository,
                               UserRepository userRepository,
                               ChatSessionRepository chatSessionRepository,
                               ChatMessageBroker chatMessageBroker,
                               EntityMapper entityMapper) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageBroker = chatMessageBroker;
        this.entityMapper = entityMapper;
    }

    /**
     * Save a new message to MySQL and broadcast to /topic/chat/{sessionId}.
     *
     * Flow:
     * 1. Validate chat session exists
     * 2. Load sender from database via email (JWT principal)
     * 3. Create and persist Message entity (status = SENT)
     * 4. Map to ChatMessageResponse
     * 5. Broadcast to /topic/chat/{sessionId}
     * 6. Return response
     */
    @Override
    public ChatMessageResponse saveAndBroadcast(SendMessageRequest request, String username) {

        // Step 1: Validate chat session
        ChatSession chatSession = chatSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chat session not found with id: " + request.getSessionId()));

        // Step 2: Load sender
        User sender = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + username));

        // Step 3: Validate content
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be blank");
        }

        // Step 4: Create and save message entity
        Message message = new Message();
        message.setContent(request.getContent().trim());
        message.setStatus(MessageStatus.SENT);
        message.setSender(sender);
        message.setChatSession(chatSession);

        Message savedMessage = messageRepository.save(message);

        // Step 5: Map to response DTO
        ChatMessageResponse response = entityMapper.toMessageResponse(savedMessage);

        // Step 6: Broadcast to all session subscribers
        chatMessageBroker.broadcastMessage(request.getSessionId(), response);

        return response;
    }

    /**
     * Update message status (SENT → DELIVERED → READ) and broadcast.
     *
     * Validates that status transitions are forward-only:
     *   SENT(1) → DELIVERED(2) → READ(3)
     * Cannot downgrade a status.
     *
     * Flow:
     * 1. Load message from database
     * 2. Validate status transition
     * 3. Update status + timestamp
     * 4. Save to MySQL
     * 5. Broadcast to /topic/status/{sessionId}
     */
    @Override
    public MessageStatusResponse updateStatusAndBroadcast(MessageStatusRequest request) {

        // Step 1: Load message
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Message not found with id: " + request.getMessageId()));

        // Step 2: Validate forward-only transition
        validateStatusTransition(message.getStatus(), request.getStatus());

        // Step 3: Update status with timestamp
        message.setStatus(request.getStatus());

        if (request.getStatus() == MessageStatus.DELIVERED) {
            message.setDeliveredAt(LocalDateTime.now());
        } else if (request.getStatus() == MessageStatus.READ) {
            message.setReadAt(LocalDateTime.now());
        }

        // Step 4: Persist
        messageRepository.save(message);

        // Step 5: Build response and broadcast
        MessageStatusResponse statusResponse = new MessageStatusResponse(
                message.getId(),
                request.getSessionId(),
                request.getStatus()
        );

        chatMessageBroker.broadcastStatus(request.getSessionId(), statusResponse);

        return statusResponse;
    }

    /**
     * Build typing indicator and broadcast to /topic/typing/{sessionId}.
     *
     * Does NOT persist to database — typing indicators are ephemeral.
     */
    @Override
    public TypingResponse broadcastTyping(TypingRequest request, String username) {

        // Load user for display name
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + username));

        TypingResponse response = new TypingResponse(
                request.getSessionId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                request.isTyping()
        );

        chatMessageBroker.broadcastTyping(request.getSessionId(), response);

        return response;
    }

    /**
     * Retrieve paginated message history for the REST API.
     * Returns Page<ChatMessageResponse> ordered by sentAt ascending.
     *
     * Usage: GET /api/messages/{sessionId}?page=0&size=20
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessageHistory(Long sessionId, Pageable pageable) {

        // Validate session exists
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException(
                    "Chat session not found with id: " + sessionId);
        }

        Page<Message> messages = messageRepository.findByChatSessionId(sessionId, pageable);
        return messages.map(entityMapper::toMessageResponse);
    }

    /**
     * Count unread messages for a user in a session.
     */
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long sessionId, String username) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + username));

        return messageRepository.countUnreadMessages(sessionId, user.getId());
    }

    // ─────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────

    /**
     * Enforce forward-only status transitions.
     * SENT(0) → DELIVERED(1) → READ(2)
     * Prevents setting a READ message back to SENT or DELIVERED.
     */
    private void validateStatusTransition(MessageStatus current, MessageStatus next) {
        int currentOrdinal = current.ordinal();
        int nextOrdinal = next.ordinal();

        if (nextOrdinal <= currentOrdinal) {
            throw new BadRequestException(
                    "Invalid status transition: cannot change from "
                    + current + " to " + next
                    + ". Status can only move forward: SENT → DELIVERED → READ");
        }
    }
}
