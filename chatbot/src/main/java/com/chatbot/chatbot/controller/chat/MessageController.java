package com.chatbot.chatbot.controller.chat;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.chatbot.dto.request.MessageStatusRequest;
import com.chatbot.chatbot.dto.response.ApiResponse;
import com.chatbot.chatbot.dto.response.ChatMessageResponse;
import com.chatbot.chatbot.dto.response.MessageStatusResponse;
import com.chatbot.chatbot.service.MessageService;

import jakarta.validation.Valid;

/**
 * Message REST Controller
 *
 * Handles HTTP-based message operations.
 * All endpoints require a valid JWT (enforced by JwtAuthenticationFilter).
 *
 * Endpoints:
 *   GET  /api/messages/{sessionId}              - paginated message history
 *   GET  /api/messages/{sessionId}/unread       - unread message count
 *   PUT  /api/messages/{messageId}/status       - manually update message status
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * GET /api/messages/{sessionId}
     *
     * Returns paginated message history for a chat session.
     * Messages are ordered by sentAt ascending (oldest → newest).
     *
     * Query parameters:
     *   page (default=0)   - zero-based page number
     *   size (default=20)  - number of messages per page
     *
     * Example:
     *   GET /api/messages/1?page=0&size=20
     *   Authorization: Bearer <JWT>
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Message history retrieved",
     *   "data": {
     *     "content": [...],
     *     "totalElements": 87,
     *     "totalPages": 5,
     *     "number": 0,
     *     "size": 20
     *   }
     * }
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> getMessageHistory(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").ascending());
        Page<ChatMessageResponse> messages = messageService.getMessageHistory(sessionId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Message history retrieved", messages));
    }

    /**
     * GET /api/messages/{sessionId}/unread
     *
     * Returns the count of unread messages in a session for the authenticated user.
     *
     * Example:
     *   GET /api/messages/1/unread
     *   Authorization: Bearer <JWT>
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Unread count retrieved",
     *   "data": 3
     * }
     */
    @GetMapping("/{sessionId}/unread")
    public ResponseEntity<ApiResponse> getUnreadCount(
            @PathVariable Long sessionId,
            Principal principal) {

        String username = principal.getName();
        Long count = messageService.getUnreadCount(sessionId, username);

        return ResponseEntity.ok(
                ApiResponse.success("Unread count retrieved", count));
    }

    /**
     * PUT /api/messages/{messageId}/status
     *
     * Manually update the status of a message via REST (HTTP fallback).
     * The primary update mechanism is WebSocket (/app/chat.status),
     * but this endpoint provides an HTTP alternative.
     *
     * Body:
     * {
     *   "messageId": 5,
     *   "sessionId": 1,
     *   "status": "READ"
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Message status updated",
     *   "data": { "messageId": 5, "sessionId": 1, "status": "READ" }
     * }
     */
    @PutMapping("/{messageId}/status")
    public ResponseEntity<ApiResponse> updateMessageStatus(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageStatusRequest request) {

        // Ensure path variable and body are consistent
        request.setMessageId(messageId);

        MessageStatusResponse response = messageService.updateStatusAndBroadcast(request);

        return ResponseEntity.ok(
                ApiResponse.success("Message status updated to " + response.getStatus(), response));
    }
}
