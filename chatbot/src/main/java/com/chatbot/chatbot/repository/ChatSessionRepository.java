package com.chatbot.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

}



