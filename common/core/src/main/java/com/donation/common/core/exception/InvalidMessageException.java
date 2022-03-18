package com.donation.common.core.exception;


public class InvalidMessageException extends Exception {
    public InvalidMessageException() {
        super("Invalid Message");
    }

    public InvalidMessageException(String message) {
        super(message);
    }
}
