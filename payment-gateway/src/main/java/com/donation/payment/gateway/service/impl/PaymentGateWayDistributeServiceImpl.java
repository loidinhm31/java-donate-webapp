package com.donation.payment.gateway.service.impl;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.exception.PayPalApiException;
import com.donation.management.model.TransactionResponse;
import com.donation.payment.gateway.service.PaymentGateWayDistributeService;
import com.donation.payment.gateway.service.PaypalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentGateWayDistributeServiceImpl implements PaymentGateWayDistributeService {

    @Autowired
    private PaypalService paypalService;

    @Override
    public TransactionResponse handleGateWay(PaymentMethodType method, String transactionId) throws PayPalApiException {
        TransactionResponse transactionResponse = null;

        switch (method) {
            case PAYPAL:
                transactionResponse = paypalService.validatePayment(transactionId);
                break;
        }

        return transactionResponse;
    }
}
