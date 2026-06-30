package com.chatbot.chatbot.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * WebSocket Event Listener
 *
 * Listens for WebSocket lifecycle events:
 *  - CONNECT    : user opens a WebSocket connection
 *  - SUBSCRIBE  : user subscribes to a topic
 *  - DISCONNECT : user closes the connection or times out
 *
 * These events are used for logging and can be extended
 * to track online presence, update agent status, etc.
 */
@Component
public class WebSocketEventListener {

    /**
     * Fired when a STOMP CONNECT frame is acknowledged.
     * At this point the user is fully connected.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String username = (accessor.getUser() != null)
                ? accessor.getUser().getName()
                : "anonymous";

        System.out.println("[WebSocket] User CONNECTED"
                + " | session=" + sessionId
                + " | user=" + username);
    }

    /**
     * Fired when a client subscribes to a STOMP destination.
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String username = (accessor.getUser() != null)
                ? accessor.getUser().getName()
                : "anonymous";

        System.out.println("[WebSocket] User SUBSCRIBED"
                + " | user=" + username
                + " | destination=" + destination);
    }

    /**
     * Fired when a STOMP DISCONNECT frame is received or connection drops.
     * Can be extended to set agent status to OFFLINE.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String username = (accessor.getUser() != null)
                ? accessor.getUser().getName()
                : "anonymous";

        System.out.println("[WebSocket] User DISCONNECTED"
                + " | session=" + sessionId
                + " | user=" + username);
    }
}
