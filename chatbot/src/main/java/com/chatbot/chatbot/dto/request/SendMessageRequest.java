package com.chatbot.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * SendMessageRequest DTO
 *
 * Received from the client when sending a chat message over WebSocket.
 *
 * STOMP frame destination: /app/chat.send
 * Body:
 * {
 *   "sessionId": 1,
 *   "content": "Hello, I need help!"
 * }
 */
public class SendMessageRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "Message content cannot be blank")
    private String content;

    // Constructors
    public SendMessageRequest() {
    }

    public SendMessageRequest(Long sessionId, String content) {
        this.sessionId = sessionId;
        this.content = content;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
