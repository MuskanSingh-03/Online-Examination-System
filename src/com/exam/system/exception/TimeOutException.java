package com.exam.system.exception;

/**
 * Thrown when an exam session's allocated time expires, triggering automated submission.
 */
public class TimeOutException extends Exception {
    public TimeOutException(String message) {
        super(message);
    }
}
