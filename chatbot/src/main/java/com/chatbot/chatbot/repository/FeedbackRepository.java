package com.chatbot.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

}