package com.chatbot.chatbot.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chatbot.chatbot.enums.ChatStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatStatus status;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    //===========================
    // Customer
    //===========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    //===========================
    // Agent
    //===========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;

    //===========================
    // Messages
    //===========================

    @OneToMany(mappedBy = "chatSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    //===========================
    // Feedback
    //===========================

    @OneToOne(mappedBy = "chatSession",
            cascade = CascadeType.ALL)
    private Feedback feedback;

    public ChatSession() {
    }

    @PrePersist
    public void onCreate() {
        startTime = LocalDateTime.now();

        if (status == null) {
            status = ChatStatus.WAITING;
        }
    }

    //===========================
    // Getters & Setters
    //===========================

    public Long getId() {
        return id;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }
}