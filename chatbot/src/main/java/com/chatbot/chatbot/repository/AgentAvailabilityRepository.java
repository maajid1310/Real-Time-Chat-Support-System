package com.chatbot.chatbot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chatbot.chatbot.entity.AgentAvailability;
import com.chatbot.chatbot.enums.AgentStatus;

public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, Long> {

    // Find availability by agent ID
    @Query("SELECT aa FROM AgentAvailability aa WHERE aa.agent.id = :agentId ORDER BY aa.lastUpdated DESC")
    Optional<AgentAvailability> findLatestByAgentId(@Param("agentId") Long agentId);
    
    // Check if agent has a specific status
    @Query("SELECT COUNT(aa) > 0 FROM AgentAvailability aa WHERE aa.agent.id = :agentId AND aa.status = :status")
    boolean existsByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") AgentStatus status);
}