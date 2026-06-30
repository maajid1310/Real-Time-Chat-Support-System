package com.chatbot.chatbot.dto.response;

import java.time.LocalDateTime;

/**
 * AgentAvailabilityResponse DTO
 * 
 * Response for agent availability operations
 */
public class AgentAvailabilityResponse {

    private Long id;
    private Long agentId;
    private String agentName;
    private String status;
    private LocalDateTime lastUpdated;

    // Constructors
    public AgentAvailabilityResponse() {
    }

    public AgentAvailabilityResponse(Long id, Long agentId, String agentName, String status, LocalDateTime lastUpdated) {
        this.id = id;
        this.agentId = agentId;
        this.agentName = agentName;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
