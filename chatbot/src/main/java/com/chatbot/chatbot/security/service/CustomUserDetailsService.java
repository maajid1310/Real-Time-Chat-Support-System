package com.chatbot.chatbot.security.service;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chatbot.chatbot.entity.User;
import com.chatbot.chatbot.repository.UserRepository;

/**
 * Custom UserDetailsService Implementation
 * 
 * Bridges Spring Security with our database.
 * Loads user from database and converts to Spring Security's UserDetails.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user by username (email in our case).
     * 
     * Called by AuthenticationManager during login.
     * Called by JwtAuthenticationFilter on each request.
     * 
     * Flow:
     * 1. Receive username (email)
     * 2. Query database via UserRepository
     * 3. If not found, throw UsernameNotFoundException
     * 4. Convert User entity to Spring Security UserDetails
     * 5. Return UserDetails with username, password, and authorities
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // Fetch user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert to Spring Security UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user)
        );
    }

    /**
     * Get user authorities (roles).
     * 
     * Converts role name to Spring Security GrantedAuthority.
     * Format: "ROLE_ADMIN", "ROLE_AGENT", "ROLE_CUSTOMER"
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String roleName = "ROLE_" + user.getRole().getRoleName();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}
