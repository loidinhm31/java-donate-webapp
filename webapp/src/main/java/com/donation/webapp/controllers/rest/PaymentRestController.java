package com.donation.webapp.controllers.rest;

import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.data.handler.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/payment/exchange-rate")
    public ResponseEntity<Float> getExchangeRate(@RequestParam String currency) throws DataProcessNotFoundException {

        Float exchangeRate = paymentService.getExchangeRate(currency);

        return ResponseEntity.ok().body(exchangeRate);
    }

}
