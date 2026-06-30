package com.chatbot.chatbot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chatbot.chatbot.entity.ChatSession;
import com.chatbot.chatbot.enums.ChatStatus;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // Find chat sessions by customer ID
    @Query("SELECT cs FROM ChatSession cs WHERE cs.customer.id = :customerId ORDER BY cs.startTime DESC")
    List<ChatSession> findByCustomerId(@Param("customerId") Long customerId);
    
    // Find chat sessions by agent ID
    @Query("SELECT cs FROM ChatSession cs WHERE cs.agent.id = :agentId ORDER BY cs.startTime DESC")
    List<ChatSession> findByAgentId(@Param("agentId") Long agentId);
    
    // Find chat sessions by status
    List<ChatSession> findByStatus(ChatStatus status);
    
    // Find active chat sessions by agent
    @Query("SELECT cs FROM ChatSession cs WHERE cs.agent.id = :agentId AND cs.status = 'ACTIVE'")
    List<ChatSession> findActiveChatsByAgentId(@Param("agentId") Long agentId);
    
    // Find waiting chat sessions (for agent assignment)
    @Query("SELECT cs FROM ChatSession cs WHERE cs.status = 'WAITING' ORDER BY cs.startTime ASC")
    List<ChatSession> findWaitingChats();
}



