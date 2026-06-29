package com.chatbot.chatbot.service;

import com.chatbot.chatbot.dto.request.LoginRequest;
import com.chatbot.chatbot.dto.request.RegisterRequest;
import com.chatbot.chatbot.dto.response.LoginResponse;

/**
 * Authentication Service Interface
 * 
 * Defines contract for authentication operations.
 */
public interface AuthService {

    /**
     * Register new user
     */
    LoginResponse register(RegisterRequest request);

    /**
     * Authenticate user and generate JWT
     */
    LoginResponse login(LoginRequest request);
}
