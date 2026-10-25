package com.exam.system.exception;

/**
 * Thrown when an invalid answer option or command selection is entered.
 */
public class InvalidOptionException extends Exception {
    public InvalidOptionException(String message) {
        super(message);
    }
}
