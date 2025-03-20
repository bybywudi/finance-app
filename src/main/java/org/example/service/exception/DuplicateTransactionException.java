package org.example.service.exception;

public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException() {
        super("Duplicate transaction detected");
    }

    public DuplicateTransactionException(String message) {
        super(message);
    }

    public DuplicateTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}