package com.chatbot.chatbot.websocket.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.chatbot.chatbot.security.jwt.JwtUtil;
import com.chatbot.chatbot.security.service.CustomUserDetailsService;

/**
 * JWT Handshake Interceptor
 *
 * Validates the JWT token during the WebSocket HTTP upgrade handshake.
 * Runs BEFORE the STOMP connection is established.
 *
 * Token can be passed in two ways:
 *   1. Query param: /ws?token=eyJhbGc...
 *   2. Authorization header: Bearer eyJhbGc...
 *
 * If valid, the authenticated user is stored in the WebSocket session attributes
 * so it is available in @MessageMapping handler methods via Principal.
 *
 * If invalid, beforeHandshake returns false and connection is rejected.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil,
                                   CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Called before the WebSocket handshake.
     * Extracts and validates the JWT token.
     * Stores the authenticated user as a Principal in session attributes.
     *
     * @return true  - allow connection
     * @return false - reject connection (401)
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            System.err.println("[WebSocket] Handshake rejected: No JWT token provided");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            System.err.println("[WebSocket] Handshake rejected: Invalid or expired JWT token");
            return false;
        }

        try {
            String username = jwtUtil.getUsernameFromToken(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Store the authenticated principal in WebSocket session attributes
            // This is later available via StompHeaderAccessor.getUser() or Principal
            attributes.put("principal", authentication);
            attributes.put("username", username);

            System.out.println("[WebSocket] Handshake accepted for user: " + username);
            return true;

        } catch (Exception e) {
            System.err.println("[WebSocket] Handshake rejected: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // No action needed after handshake
    }

    /**
     * Extract JWT from query parameter or Authorization header.
     *
     * SockJS HTTP upgrade cannot always send custom headers,
     * so the token is accepted from the ?token= query parameter as well.
     */
    private String extractToken(ServerHttpRequest request) {

        // 1. Try query parameter first: /ws?token=eyJhbGc...
        String query = request.getURI().getQuery();
        if (StringUtils.hasText(query)) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    if (StringUtils.hasText(token)) {
                        return token;
                    }
                }
            }
        }

        // 2. Try Authorization header: Bearer eyJhbGc...
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
