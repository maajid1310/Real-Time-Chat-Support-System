package com.chatbot.chatbot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.chatbot.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}