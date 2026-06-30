package com.chatbot.chatbot.exception;

/**
 * ForbiddenException
 * 
 * Thrown when a user attempts to access a resource they don't have permission for.
 * Results in HTTP 403 Forbidden response.
 * 
 * Example scenarios:
 * - Customer trying to access admin endpoints
 * - Agent trying to view another agent's private data
 * - User trying to modify resources they don't own
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
