package com.chatbot.chatbot.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 50)
    private String firstName;

    @Column(nullable = false,length = 50)
    private String lastName;

    @Column(nullable = false,unique = true,length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 15)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //=============================
    // Role Relationship
    //=============================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    //=============================
    // Chat Relationship
    //=============================

    @OneToMany(mappedBy = "customer")
    private List<ChatSession> customerChats = new ArrayList<>();

    @OneToMany(mappedBy = "agent")
    private List<ChatSession> agentChats = new ArrayList<>();

    //=============================
    // Notifications
    //=============================

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications = new ArrayList<>();

    //=============================
    // Audit Logs
    //=============================

    @OneToMany(mappedBy = "user")
    private List<AuditLog> auditLogs = new ArrayList<>();

    //=============================
    // Agent Availability
    //=============================

    @OneToMany(mappedBy = "agent")
    private List<AgentAvailability> availability = new ArrayList<>();

    public User() {
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    //=============================
    // Getters and Setters
    //=============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id=id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName=firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName=lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email=email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password=password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone=phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active=active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role=role;
    }

    public List<ChatSession> getCustomerChats() {
        return customerChats;
    }

    public List<ChatSession> getAgentChats() {
        return agentChats;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public List<AgentAvailability> getAvailability() {
        return availability;
    }
}