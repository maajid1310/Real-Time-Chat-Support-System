package com.chatbot.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}