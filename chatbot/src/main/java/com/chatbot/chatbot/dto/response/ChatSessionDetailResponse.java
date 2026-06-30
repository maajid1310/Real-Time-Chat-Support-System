package com.chatbot.chatbot.dto.response;

import java.time.LocalDateTime;

/**
 * ChatSessionResponse DTO
 * 
 * Enhanced response for chat session operations
 */
public class ChatSessionDetailResponse {

    private Long id;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // Customer details
    private Long customerId;
    private String customerName;
    private String customerEmail;
    
    // Agent details
    private Long agentId;
    private String agentName;
    private String agentEmail;
    
    // Session statistics
    private Integer messageCount;

    // Constructors
    public ChatSessionDetailResponse() {
    }

    public ChatSessionDetailResponse(Long id, String status, LocalDateTime startTime, LocalDateTime endTime,
                                      Long customerId, String customerName, String customerEmail,
                                      Long agentId, String agentName, String agentEmail, Integer messageCount) {
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.agentId = agentId;
        this.agentName = agentName;
        this.agentEmail = agentEmail;
        this.messageCount = messageCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}
