package com.chatbot.chatbot.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chatbot.chatbot.dto.response.ChatMessageResponse;
import com.chatbot.chatbot.dto.response.MessageStatusResponse;
import com.chatbot.chatbot.dto.response.TypingResponse;

/**
 * Chat Message Broker
 *
 * Central broadcast hub for all real-time WebSocket messages.
 * Wraps SimpMessagingTemplate and provides strongly-typed broadcast methods.
 *
 * Topics used:
 *   /topic/chat/{sessionId}    - new chat messages
 *   /topic/typing/{sessionId}  - typing indicators
 *   /topic/status/{sessionId}  - SENT / DELIVERED / READ updates
 */
@Component
public class ChatMessageBroker {

    private static final String CHAT_TOPIC   = "/topic/chat/";
    private static final String TYPING_TOPIC = "/topic/typing/";
    private static final String STATUS_TOPIC = "/topic/status/";

    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageBroker(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast a new chat message to all subscribers of the session.
     * Both the sender and the recipient are subscribed to /topic/chat/{sessionId}.
     *
     * @param sessionId the chat session ID
     * @param response  the message payload to broadcast
     */
    public void broadcastMessage(Long sessionId, ChatMessageResponse response) {
        String destination = CHAT_TOPIC + sessionId;
        messagingTemplate.convertAndSend(destination, response);
        System.out.println("[Broker] Message broadcast → " + destination
                + " | messageId=" + response.getMessageId());
    }

    /**
     * Broadcast a typing indicator to all subscribers of the session.
     *
     * @param sessionId the chat session ID
     * @param response  the typing payload (who is typing, boolean flag)
     */
    public void broadcastTyping(Long sessionId, TypingResponse response) {
        String destination = TYPING_TOPIC + sessionId;
        messagingTemplate.convertAndSend(destination, response);
    }

    /**
     * Broadcast a message status update (SENT → DELIVERED → READ).
     *
     * @param sessionId the chat session ID
     * @param response  the status payload containing messageId and new status
     */
    public void broadcastStatus(Long sessionId, MessageStatusResponse response) {
        String destination = STATUS_TOPIC + sessionId;
        messagingTemplate.convertAndSend(destination, response);
        System.out.println("[Broker] Status broadcast → " + destination
                + " | messageId=" + response.getMessageId()
                + " | status=" + response.getStatus());
    }
}
