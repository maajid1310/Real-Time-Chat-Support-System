package com.chatbot.chatbot.dto.request;

import com.chatbot.chatbot.enums.MessageStatus;

import jakarta.validation.constraints.NotNull;

/**
 * MessageStatusRequest DTO
 *
 * Received from the client to update the status of a message.
 *
 * STOMP frame destination: /app/chat.status
 * Body:
 * {
 *   "messageId": 5,
 *   "sessionId": 1,
 *   "status": "DELIVERED"
 * }
 *
 * Valid transitions:
 *   SENT → DELIVERED → READ
 */
public class MessageStatusRequest {

    @NotNull(message = "Message ID is required")
    private Long messageId;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Status is required")
    private MessageStatus status;

    // Constructors
    public MessageStatusRequest() {
    }

    public MessageStatusRequest(Long messageId, Long sessionId, MessageStatus status) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.status = status;
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
}
