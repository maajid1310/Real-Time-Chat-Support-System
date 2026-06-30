package com.chatbot.chatbot.websocket.interceptor;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * WebSocket Auth Channel Interceptor
 *
 * Bridges the authenticated Principal stored in WebSocket session attributes
 * (set by JwtHandshakeInterceptor) into the STOMP message headers.
 *
 * Without this, Principal is null inside @MessageMapping methods
 * even though the handshake was authenticated.
 *
 * Registered in WebSocketConfig via configureClientInboundChannel().
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Only process on CONNECT — set the user principal from session attributes
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

            if (sessionAttributes != null) {
                Principal principal = (Principal) sessionAttributes.get("principal");

                if (principal instanceof UsernamePasswordAuthenticationToken auth) {
                    accessor.setUser(auth);
                }
            }
        }

        return message;
    }
}
