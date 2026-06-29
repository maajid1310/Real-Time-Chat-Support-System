package com.chatbot.chatbot.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.chatbot.chatbot.security.filter.JwtAuthenticationFilter;
import com.chatbot.chatbot.security.jwt.JwtAuthenticationEntryPoint;

/**
 * Spring Security Configuration Class
 * 
 * This class configures the security layer for the entire application.
 * It defines authentication rules, password encoding, and JWT filter chain.
 * 
 * @author Chat Support System
 * @version 1.0
 * @since 2026
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Constructor-based Dependency Injection
     * 
     * Spring will automatically inject these dependencies when creating this bean.
     * Constructor injection is preferred over field injection for better testability.
     * 
     * @param jwtAuthenticationFilter - Custom filter to process JWT tokens
     * @param jwtAuthenticationEntryPoint - Handles unauthorized access attempts
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * Security Filter Chain Bean
     * 
     * This is the MAIN security configuration method.
     * It defines which endpoints are public and which require authentication.
     * It also configures CORS, CSRF, session management, and the JWT filter.
     * 
     * @param http - HttpSecurity object to configure web-based security
     * @return SecurityFilterChain - The configured security filter chain
     * @throws Exception - If configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
            // CSRF Configuration
            // Disable CSRF (Cross-Site Request Forgery) protection
            // Safe for stateless REST APIs using JWT tokens
            // CSRF protection is needed only for session-based authentication
            .csrf(csrf -> csrf.disable())
            
            // CORS Configuration
            // Enable Cross-Origin Resource Sharing
            // Allows frontend (React) running on different port to communicate
            // Actual CORS rules are defined in CorsConfig.java
            .cors(cors -> cors.configure(http))
            
            // Authorization Rules
            // Define which endpoints are PUBLIC and which are PROTECTED
            .authorizeHttpRequests(auth -> auth
                
                // PUBLIC Endpoints - No authentication required
                // These endpoints can be accessed without JWT token
                .requestMatchers(
                    "/api/auth/**",           // Login, Register endpoints
                    "/v3/api-docs/**",        // Swagger API documentation
                    "/swagger-ui/**",         // Swagger UI interface
                    "/swagger-ui.html",       // Swagger UI HTML page
                    "/ws/**"                  // WebSocket connections
                ).permitAll()
                
                // PROTECTED Endpoints - Authentication required
                // All other endpoints require valid JWT token
                .anyRequest().authenticated()
            )
            
            // Exception Handling
            // Configure custom entry point for unauthorized access
            // When user tries to access protected resource without valid token,
            // JwtAuthenticationEntryPoint will return 401 Unauthorized response
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Session Management
            // Set session policy to STATELESS
            // Server will NOT create or use HTTP sessions
            // Each request must contain JWT token for authentication
            // This makes the API scalable and stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        // Add JWT Filter
        // Register our custom JWT filter BEFORE Spring Security's default UsernamePasswordAuthenticationFilter
        // This ensures JWT validation happens FIRST before any other authentication mechanism
        // Filter order: JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter -> Other filters
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Build and return the configured SecurityFilterChain
        return http.build();
    }

    /**
     * Password Encoder Bean
     * 
     * BCryptPasswordEncoder is used to hash passwords before storing in database.
     * BCrypt is a one-way hashing algorithm designed specifically for passwords.
     * 
     * Why BCrypt?
     * - Automatically generates salt (random data added to password)
     * - Slow by design (prevents brute-force attacks)
     * - Industry-standard for password hashing
     * - Cannot be reversed (one-way encryption)
     * 
     * Example:
     * Plain password: "password123"
     * BCrypt hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * 
     * @return PasswordEncoder - BCrypt password encoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager Bean
     * 
     * AuthenticationManager is responsible for processing authentication requests.
     * It uses UserDetailsService to load user details and PasswordEncoder to verify passwords.
     * 
     * Flow:
     * 1. User submits login credentials (email + password)
     * 2. AuthenticationManager receives the request
     * 3. It calls CustomUserDetailsService to load user from database
     * 4. It uses PasswordEncoder to compare passwords
     * 5. If valid, returns authenticated user object
     * 6. If invalid, throws AuthenticationException
     * 
     * This bean is required by AuthService for login functionality.
     * 
     * @param authenticationConfiguration - Spring's authentication configuration
     * @return AuthenticationManager - The authentication manager instance
     * @throws Exception - If unable to get authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
