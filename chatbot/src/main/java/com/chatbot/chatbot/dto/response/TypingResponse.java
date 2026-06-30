package com.chatbot.chatbot.dto.response;

import java.time.LocalDateTime;

/**
 * TypingResponse DTO
 *
 * Broadcast to /topic/typing/{sessionId} when a user is typing.
 */
public class TypingResponse {

    private Long sessionId;
    private Long userId;
    private String username;
    private String displayName;
    private boolean typing;
    private LocalDateTime timestamp;

    // Constructors
    public TypingResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public TypingResponse(Long sessionId, Long userId, String username,
                          String displayName, boolean typing) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.typing = typing;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
