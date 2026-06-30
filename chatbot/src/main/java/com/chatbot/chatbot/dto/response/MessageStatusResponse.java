package com.chatbot.chatbot.dto.response;

import java.time.LocalDateTime;

import com.chatbot.chatbot.enums.MessageStatus;

/**
 * MessageStatusResponse DTO
 *
 * Broadcast to /topic/status/{sessionId} when a message status changes.
 * Transitions: SENT → DELIVERED → READ
 */
public class MessageStatusResponse {

    private Long messageId;
    private Long sessionId;
    private MessageStatus status;
    private LocalDateTime updatedAt;

    // Constructors
    public MessageStatusResponse() {
        this.updatedAt = LocalDateTime.now();
    }

    public MessageStatusResponse(Long messageId, Long sessionId, MessageStatus status) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
