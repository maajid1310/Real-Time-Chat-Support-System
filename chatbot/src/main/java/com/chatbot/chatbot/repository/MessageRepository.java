package com.chatbot.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

}