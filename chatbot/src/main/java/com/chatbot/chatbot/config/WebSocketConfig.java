package com.chatbot.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.chatbot.chatbot.websocket.interceptor.JwtHandshakeInterceptor;
import com.chatbot.chatbot.websocket.interceptor.WebSocketAuthChannelInterceptor;

/**
 * WebSocket Configuration
 *
 * Configures STOMP over WebSocket with SockJS fallback.
 *
 * Two-layer JWT security:
 *  Layer 1: JwtHandshakeInterceptor  → validates JWT during HTTP upgrade
 *  Layer 2: WebSocketAuthChannelInterceptor → propagates Principal into STOMP frames
 *
 * Topics:
 *   /topic/chat/{sessionId}    - live chat messages
 *   /topic/typing/{sessionId}  - typing indicators
 *   /topic/status/{sessionId}  - message status (SENT/DELIVERED/READ)
 *
 * Client sends to:
 *   /app/chat.send    - send message
 *   /app/chat.typing  - typing indicator
 *   /app/chat.status  - status update
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;

    @Value("${websocket.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor,
                           WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.webSocketAuthChannelInterceptor = webSocketAuthChannelInterceptor;
    }

    /**
     * Register STOMP endpoint /ws with SockJS fallback.
     * JWT validated in JwtHandshakeInterceptor.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS();
    }

    /**
     * Configure message broker.
     *
     * /app    → routes to @MessageMapping controller methods
     * /topic  → simple in-memory pub-sub broker (broadcast)
     * /user   → user-specific destinations
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/user");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Register channel interceptor on the inbound channel.
     * This moves the JWT Principal from the handshake session attributes
     * into the STOMP message so @MessageMapping methods can inject Principal.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }
}
