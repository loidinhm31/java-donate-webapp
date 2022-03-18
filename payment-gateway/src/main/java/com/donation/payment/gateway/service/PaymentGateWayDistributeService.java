package com.donation.payment.gateway.service;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.exception.PayPalApiException;
import com.donation.management.model.TransactionResponse;

public interface PaymentGateWayDistributeService {
    TransactionResponse handleGateWay(PaymentMethodType method, String transactionId) throws PayPalApiException;
}
