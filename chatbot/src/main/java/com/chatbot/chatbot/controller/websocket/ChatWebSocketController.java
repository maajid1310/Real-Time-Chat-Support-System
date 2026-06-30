package com.chatbot.chatbot.controller.websocket;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.chatbot.chatbot.dto.request.MessageStatusRequest;
import com.chatbot.chatbot.dto.request.SendMessageRequest;
import com.chatbot.chatbot.dto.request.TypingRequest;
import com.chatbot.chatbot.service.MessageService;

/**
 * Chat WebSocket Controller
 *
 * Handles all inbound STOMP messages from clients.
 * The @MessageMapping prefix is /app (configured in WebSocketConfig).
 *
 * Endpoints:
 *   /app/chat.send    → receive a new message, save + broadcast to /topic/chat/{sessionId}
 *   /app/chat.typing  → receive typing event, broadcast to /topic/typing/{sessionId}
 *   /app/chat.status  → receive status update, persist + broadcast to /topic/status/{sessionId}
 *
 * The Principal is injected automatically by Spring from the WebSocket session.
 * It is populated with the JWT-authenticated user set in JwtHandshakeInterceptor.
 */
@Controller
public class ChatWebSocketController {

    private final MessageService messageService;

    public ChatWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Handle incoming chat message.
     *
     * Client sends STOMP frame to: /app/chat.send
     * Payload:
     * {
     *   "sessionId": 1,
     *   "content": "Hello, I need help!"
     * }
     *
     * After saving to DB, message is broadcast to /topic/chat/{sessionId}
     * so BOTH customer and agent receive it instantly.
     *
     * @param request   the message payload
     * @param principal the JWT-authenticated sender (injected from STOMP session)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {

        if (principal == null) {
            System.err.println("[WS] sendMessage rejected: unauthenticated request");
            return;
        }

        String username = principal.getName();
        System.out.println("[WS] Message received from: " + username
                + " | sessionId=" + request.getSessionId());

        messageService.saveAndBroadcast(request, username);
    }

    /**
     * Handle typing indicator.
     *
     * Client sends STOMP frame to: /app/chat.typing
     * Payload:
     * {
     *   "sessionId": 1,
     *   "typing": true
     * }
     *
     * Broadcast to /topic/typing/{sessionId} — not persisted to DB.
     * The recipient's UI shows "X is typing..." and auto-hides after timeout.
     *
     * @param request   the typing payload
     * @param principal the JWT-authenticated user
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingRequest request, Principal principal) {

        if (principal == null) {
            return;
        }

        String username = principal.getName();
        messageService.broadcastTyping(request, username);
    }

    /**
     * Handle message status update.
     *
     * Client sends STOMP frame to: /app/chat.status
     * Payload:
     * {
     *   "messageId": 5,
     *   "sessionId": 1,
     *   "status": "DELIVERED"
     * }
     *
     * Status is persisted to MySQL and broadcast to /topic/status/{sessionId}.
     * Valid transitions: SENT → DELIVERED → READ
     *
     * @param request   the status update payload
     * @param principal the JWT-authenticated user
     */
    @MessageMapping("/chat.status")
    public void updateStatus(@Payload MessageStatusRequest request, Principal principal) {

        if (principal == null) {
            System.err.println("[WS] updateStatus rejected: unauthenticated request");
            return;
        }

        System.out.println("[WS] Status update: messageId=" + request.getMessageId()
                + " | status=" + request.getStatus()
                + " | by=" + principal.getName());

        messageService.updateStatusAndBroadcast(request);
    }
}
