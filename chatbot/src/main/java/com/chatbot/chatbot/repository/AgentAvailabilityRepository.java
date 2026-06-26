package com.chatbot.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.AgentAvailability;

public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, Long> {

}