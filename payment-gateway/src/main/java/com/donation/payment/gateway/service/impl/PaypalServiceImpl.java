package com.donation.payment.gateway.service.impl;

import com.donation.common.core.exception.PayPalApiException;
import com.donation.management.model.PaypalResponse;
import com.donation.management.model.TransactionResponse;
import com.donation.payment.gateway.service.PaypalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class PaypalServiceImpl implements PaypalService {
    @Value("${paypal.payment-api}")
    String paypalOrderApi;

    @Value("${paypal.client-id}")
    String paypalClientId;

    @Value("${paypal.client-secret}")
    String paypalClientSecret;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public TransactionResponse validatePayment(String transactionId) throws PayPalApiException {
        TransactionResponse transactionResponse = processCaptureResult(transactionId);

        if (transactionResponse.getStatus().equalsIgnoreCase("COMPLETED")) {
            return transactionResponse;
        }
        return null;
    }

    private TransactionResponse processCaptureResult(String transactionId) throws PayPalApiException {
        ResponseEntity<PaypalResponse> response = getPaypalPaymentCapture(transactionId);

        HttpStatus statusCode = response.getStatusCode();

        if (!statusCode.equals(HttpStatus.OK)) {
            handlerErrorForPaypalResponse(statusCode);
        }

        PaypalResponse paypalResponse = response.getBody();

        return TransactionResponse.builder()
                .id(paypalResponse.getId())
                .status(paypalResponse.getStatus())
                .amount(paypalResponse.getPaypalBreakDown().getNetAmount())
                .build();
    }

    private ResponseEntity<PaypalResponse> getPaypalPaymentCapture(String orderId) {

        String requestUrl = paypalOrderApi + orderId;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(paypalClientId, paypalClientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<PaypalResponse> response = null;
        try {
            response = restTemplate.exchange(
                    requestUrl, HttpMethod.GET, request, PaypalResponse.class);
        } catch (Exception e) {
            log.error("Error at call paypal transaction", e);
        }
        return response;
    }

    private void handlerErrorForPaypalResponse(HttpStatus statusCode) throws PayPalApiException {
        String message;
        switch (statusCode) {
            case NOT_FOUND:
                message = "Transaction ID not found";
                break;
            case BAD_REQUEST:
                message = "Bad Request to PayPal Checkout API";
                break;
            case INTERNAL_SERVER_ERROR:
                message = "PayPal server error";
                break;
            default:
                message = "PayPal returned non-OK status code";
        }
        throw new PayPalApiException(message);
    }
}
