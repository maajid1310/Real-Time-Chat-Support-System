package com.chatbot.chatbot.exception;

/**
 * Unauthorized Exception
 * 
 * Thrown when user attempts unauthorized access.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
