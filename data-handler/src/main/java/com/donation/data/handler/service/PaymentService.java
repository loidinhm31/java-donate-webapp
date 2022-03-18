package com.donation.data.handler.service;

import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.core.exception.PayPalApiException;
import com.donation.management.model.DonateInformation;

public interface PaymentService {

    /**
     * Check Project exist in the system for payment process
     *
     * @param donateInformation
     * @return
     * @throws InvalidMessageException
     */
    DonateInformation checkProjectExist(DonateInformation donateInformation) throws InvalidMessageException;

    /**
     * Process payment to check whether it has valid information to complete payment
     *
     * @param donateInformation
     * @throws PayPalApiException
     * @throws InvalidMessageException
     */
    void processPayment(DonateInformation donateInformation) throws PayPalApiException, InvalidMessageException, DataProcessNotFoundException;

    /**
     * Fetch exchange rate from external api in converting currency for PayPal
     *
     * @param currency
     * @return
     * @throws DataProcessNotFoundException
     */
    Float getExchangeRate(String currency) throws DataProcessNotFoundException;
}
