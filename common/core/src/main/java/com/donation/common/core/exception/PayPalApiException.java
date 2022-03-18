package com.donation.common.core.exception;

public class PayPalApiException extends Exception {
    public PayPalApiException() {
        super("Paypal API Error Exception");
    }

    public PayPalApiException(String message) {
        super(message);
    }
}
