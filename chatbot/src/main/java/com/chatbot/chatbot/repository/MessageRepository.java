package com.chatbot.chatbot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chatbot.chatbot.entity.Message;
import com.chatbot.chatbot.enums.MessageStatus;

/**
 * Message Repository
 *
 * Provides all database operations for the messages table.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages in a session ordered by sentAt ascending (oldest first).
     * Used for loading full chat history.
     */
    @Query("SELECT m FROM Message m WHERE m.chatSession.id = :sessionId ORDER BY m.sentAt ASC")
    List<Message> findByChatSessionIdOrderBySentAtAsc(@Param("sessionId") Long sessionId);

    /**
     * Find messages in a session with pagination (oldest first).
     * Used for GET /api/messages/{sessionId}?page=0&size=20
     */
    @Query(value = "SELECT m FROM Message m WHERE m.chatSession.id = :sessionId ORDER BY m.sentAt ASC",
           countQuery = "SELECT COUNT(m) FROM Message m WHERE m.chatSession.id = :sessionId")
    Page<Message> findByChatSessionId(@Param("sessionId") Long sessionId, Pageable pageable);

    /**
     * Find the latest N messages in a session (newest first) for initial load.
     */
    @Query("SELECT m FROM Message m WHERE m.chatSession.id = :sessionId ORDER BY m.sentAt DESC")
    Page<Message> findLatestByChatSessionId(@Param("sessionId") Long sessionId, Pageable pageable);

    /**
     * Count unread messages in a session for a specific recipient.
     * Unread = status is SENT or DELIVERED but not READ.
     */
    @Query("SELECT COUNT(m) FROM Message m " +
           "WHERE m.chatSession.id = :sessionId " +
           "AND m.sender.id != :userId " +
           "AND m.status != com.chatbot.chatbot.enums.MessageStatus.READ")
    Long countUnreadMessages(@Param("sessionId") Long sessionId,
                             @Param("userId") Long userId);

    /**
     * Find all unread messages in a session for a specific recipient.
     * Used to mark messages as DELIVERED or READ in bulk.
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.chatSession.id = :sessionId " +
           "AND m.sender.id != :userId " +
           "AND m.status != com.chatbot.chatbot.enums.MessageStatus.READ " +
           "ORDER BY m.sentAt ASC")
    List<Message> findUnreadMessages(@Param("sessionId") Long sessionId,
                                     @Param("userId") Long userId);

    /**
     * Find all messages by a specific sender in a session.
     */
    @Query("SELECT m FROM Message m WHERE m.chatSession.id = :sessionId AND m.sender.id = :senderId ORDER BY m.sentAt ASC")
    List<Message> findByChatSessionIdAndSenderId(@Param("sessionId") Long sessionId,
                                                 @Param("senderId") Long senderId);

    /**
     * Count total messages in a session.
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatSession.id = :sessionId")
    Long countByChatSessionId(@Param("sessionId") Long sessionId);

    /**
     * Find all messages by status in a session.
     */
    @Query("SELECT m FROM Message m WHERE m.chatSession.id = :sessionId AND m.status = :status ORDER BY m.sentAt ASC")
    List<Message> findByChatSessionIdAndStatus(@Param("sessionId") Long sessionId,
                                               @Param("status") MessageStatus status);
}
