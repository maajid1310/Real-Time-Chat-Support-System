package com.chatbot.chatbot.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.chatbot.dto.request.LoginRequest;
import com.chatbot.chatbot.dto.request.RegisterRequest;
import com.chatbot.chatbot.dto.response.LoginResponse;
import com.chatbot.chatbot.entity.Role;
import com.chatbot.chatbot.entity.User;
import com.chatbot.chatbot.exception.BadRequestException;
import com.chatbot.chatbot.exception.ResourceNotFoundException;
import com.chatbot.chatbot.repository.RoleRepository;
import com.chatbot.chatbot.repository.UserRepository;
import com.chatbot.chatbot.security.jwt.JwtUtil;
import com.chatbot.chatbot.service.AuthService;

/**
 * Authentication Service Implementation
 * 
 * Handles user registration and login with JWT generation.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register new user
     * 
     * Flow:
     * 1. Validate email uniqueness
     * 2. Fetch role from database
     * 3. Encode password
     * 4. Create user entity
     * 5. Save to database
     * 6. Generate JWT token
     * 7. Return login response
     */
    @Override
    public LoginResponse register(RegisterRequest request) {
        
        // Step 1: Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Step 2: Fetch role from database
        Role role = roleRepository.findByRoleName(request.getRole().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        // Step 3: Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setActive(true);

        // Step 4: Save user
        User savedUser = userRepository.save(user);

        // Step 5: Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Step 6: Return response
        return new LoginResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole().getRoleName()
        );
    }

    /**
     * Login user
     * 
     * Flow:
     * 1. Authenticate credentials using AuthenticationManager
     * 2. If valid, fetch user from database
     * 3. Generate JWT token
     * 4. Return login response with token and user info
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        
        // Step 1: Authenticate user credentials
        // AuthenticationManager uses CustomUserDetailsService to load user
        // and PasswordEncoder to verify password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Step 2: Fetch user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        // Step 3: Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Step 4: Return response
        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getRoleName()
        );
    }
}
