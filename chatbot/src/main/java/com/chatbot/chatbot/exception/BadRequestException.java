package com.chatbot.chatbot.exception;

/**
 * Bad Request Exception
 * 
 * Thrown when request data is invalid (e.g., email already exists).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
