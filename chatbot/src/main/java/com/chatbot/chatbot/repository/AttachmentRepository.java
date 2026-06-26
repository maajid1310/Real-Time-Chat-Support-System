package com.chatbot.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

}