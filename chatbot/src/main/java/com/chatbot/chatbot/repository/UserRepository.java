package com.chatbot.chatbot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chatbot.chatbot.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    // Find users by role name
    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    // Find active users by role
    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName AND u.active = true")
    List<User> findActiveUsersByRoleName(@Param("roleName") String roleName);
    
    // Count users by role
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.roleName = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);
}