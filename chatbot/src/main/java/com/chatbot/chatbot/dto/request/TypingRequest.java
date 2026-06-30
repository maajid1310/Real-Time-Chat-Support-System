package com.chatbot.chatbot.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * TypingRequest DTO
 *
 * Received from the client to broadcast a typing indicator.
 *
 * STOMP frame destination: /app/chat.typing
 * Body:
 * {
 *   "sessionId": 1,
 *   "typing": true
 * }
 */
public class TypingRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private boolean typing;

    // Constructors
    public TypingRequest() {
    }

    public TypingRequest(Long sessionId, boolean typing) {
        this.sessionId = sessionId;
        this.typing = typing;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
