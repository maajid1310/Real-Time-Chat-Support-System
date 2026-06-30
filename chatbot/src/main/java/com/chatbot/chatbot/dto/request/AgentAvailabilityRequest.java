package com.chatbot.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * AgentAvailabilityRequest DTO
 * 
 * Used by AGENT to update their availability status
 */
public class AgentAvailabilityRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "AVAILABLE|BUSY|OFFLINE", message = "Status must be AVAILABLE, BUSY, or OFFLINE")
    private String status;

    // Constructors
    public AgentAvailabilityRequest() {
    }

    public AgentAvailabilityRequest(String status) {
        this.status = status;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
