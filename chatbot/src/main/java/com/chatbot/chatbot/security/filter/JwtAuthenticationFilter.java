package com.chatbot.chatbot.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chatbot.chatbot.security.jwt.JwtUtil;
import com.chatbot.chatbot.security.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter
 * 
 * Intercepts every HTTP request and validates JWT token.
 * Executes BEFORE Spring Security's authentication filters.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Main filter logic executed on every request.
     * 
     * Flow:
     * 1. Extract Authorization header
     * 2. Extract JWT token
     * 3. Validate token
     * 4. Load user details
     * 5. Set authentication in SecurityContext
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from request header
            String jwt = extractJwtFromRequest(request);

            // Step 2: Validate and process token
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {

                // Step 3: Get username from token
                String username = jwtUtil.getUsernameFromToken(jwt);

                // Step 4: Load user details from database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Step 5: Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                        );

                // Step 6: Set request details
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 7: Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            System.err.println("Cannot set user authentication: " + e.getMessage());
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     * 
     * Header format: "Bearer <token>"
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
