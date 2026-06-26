package com.chatbot.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}