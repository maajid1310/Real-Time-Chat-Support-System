package com.chatbot.chatbot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.chatbot.chatbot.dto.request.MessageStatusRequest;
import com.chatbot.chatbot.dto.request.SendMessageRequest;
import com.chatbot.chatbot.dto.request.TypingRequest;
import com.chatbot.chatbot.dto.response.ChatMessageResponse;
import com.chatbot.chatbot.dto.response.MessageStatusResponse;
import com.chatbot.chatbot.dto.response.TypingResponse;

/**
 * Message Service Interface
 *
 * Defines the contract for all real-time messaging operations.
 */
public interface MessageService {

    /**
     * Save a new message to MySQL and broadcast it to the session topic.
     *
     * @param request   contains sessionId and content
     * @param username  the authenticated sender's email (from JWT Principal)
     * @return ChatMessageResponse with full message details
     */
    ChatMessageResponse saveAndBroadcast(SendMessageRequest request, String username);

    /**
     * Update the status of a message (SENT → DELIVERED → READ)
     * and broadcast the status update to the session status topic.
     *
     * @param request  contains messageId, sessionId, and new status
     * @return MessageStatusResponse with updated status
     */
    MessageStatusResponse updateStatusAndBroadcast(MessageStatusRequest request);

    /**
     * Build and broadcast a typing indicator to the session typing topic.
     *
     * @param request   contains sessionId and typing flag
     * @param username  the authenticated user's email (from JWT Principal)
     * @return TypingResponse with user info and typing state
     */
    TypingResponse broadcastTyping(TypingRequest request, String username);

    /**
     * Retrieve paginated message history for a session.
     * Used by the REST API: GET /api/messages/{sessionId}?page=0&size=20
     *
     * @param sessionId the chat session ID
     * @param pageable  Spring Pageable (page number, size, sort)
     * @return Page of ChatMessageResponse
     */
    Page<ChatMessageResponse> getMessageHistory(Long sessionId, Pageable pageable);

    /**
     * Get count of unread messages in a session for the given user.
     *
     * @param sessionId the chat session ID
     * @param username  the authenticated user's email
     * @return count of unread messages
     */
    Long getUnreadCount(Long sessionId, String username);
}
