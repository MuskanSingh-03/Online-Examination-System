package com.exam.system.exception;

/**
 * Thrown when user authentication fails due to incorrect password or empty credentials.
 */
public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
