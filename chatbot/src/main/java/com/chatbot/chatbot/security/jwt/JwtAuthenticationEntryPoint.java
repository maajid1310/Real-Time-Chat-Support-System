package com.chatbot.chatbot.security.jwt;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Entry Point
 * 
 * Handles unauthorized access attempts.
 * Returns 401 Unauthorized when user tries to access protected resource without valid JWT.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Invoked when user attempts to access protected resource without authentication.
     * 
     * @param request - HTTP request
     * @param response - HTTP response
     * @param authException - Authentication exception details
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" 
                + authException.getMessage() + "\"}");
    }
}
