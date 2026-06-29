package com.chatbot.chatbot.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.chatbot.dto.request.LoginRequest;
import com.chatbot.chatbot.dto.request.RegisterRequest;
import com.chatbot.chatbot.dto.response.ApiResponse;
import com.chatbot.chatbot.dto.response.LoginResponse;
import com.chatbot.chatbot.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Authentication Controller
 * 
 * Handles user registration and login endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs for registration and login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register new user
     * 
     * Endpoint: POST /api/auth/register
     * 
     * Request Body:
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john@example.com",
     *   "password": "password123",
     *   "phone": "1234567890",
     *   "role": "CUSTOMER"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "User registered successfully",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzUxMiJ9...",
     *     "type": "Bearer",
     *     "userId": 1,
     *     "email": "john@example.com",
     *     "firstName": "John",
     *     "lastName": "Doe",
     *     "role": "CUSTOMER"
     *   }
     * }
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account and returns JWT token")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        
        LoginResponse response = authService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Login user
     * 
     * Endpoint: POST /api/auth/login
     * 
     * Request Body:
     * {
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzUxMiJ9...",
     *     "type": "Bearer",
     *     "userId": 1,
     *     "email": "john@example.com",
     *     "firstName": "John",
     *     "lastName": "Doe",
     *     "role": "CUSTOMER"
     *   }
     * }
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        
        LoginResponse response = authService.login(request);
        
        return ResponseEntity
                .ok(ApiResponse.success("Login successful", response));
    }
}
