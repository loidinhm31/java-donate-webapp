package com.donation.payment.gateway.service;

import com.donation.common.core.exception.PayPalApiException;
import com.donation.management.model.TransactionResponse;

public interface PaypalService {
    TransactionResponse validatePayment(String orderId) throws PayPalApiException;
}
