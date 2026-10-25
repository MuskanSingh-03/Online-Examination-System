package com.exam.system.exception;

/**
 * Thrown when an account lookup cannot find the specified username.
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
