package com.donation.webapp.controllers;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.core.exception.PayPalApiException;
import com.donation.data.handler.service.PaymentService;
import com.donation.management.model.DonateInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/donation")
    public String handleDonateInformation(Model theModel, DonateInformation donateInformation) {

        try {
            donateInformation = paymentService.checkProjectExist(donateInformation);

            theModel.addAttribute("donationInformation", donateInformation);
        } catch (InvalidMessageException e) {
            theModel.addAttribute("message", e.getMessage());
            return "views/message";
        }

        return "views/payment/payment";
    }

    @PostMapping("/process-transaction")
    public String processPayPalTransaction(DonateInformation donateInformation, Model theModel) {

        String pageTitle;
        String message;

        try {
            paymentService.processPayment(donateInformation);
            pageTitle = "Successfully Donation";
            message = "Thank you for your donation";
        } catch (Exception e) {
            pageTitle = "Donate Failure";
            message = "ERROR: Transaction failed due to error: " + e.getMessage();
        }

        theModel.addAttribute("pageTitle", pageTitle);
        theModel.addAttribute("title", pageTitle);
        theModel.addAttribute("message", message);
        theModel.addAttribute("profile", activeProfile);
        return "views/message";
    }

    @PostMapping("/process-manual")
    public String processManualTransfer(DonateInformation donateInformation, Model theModel) {
        String pageTitle;
        String message;
        try {
            donateInformation.setMethodType(PaymentMethodType.MANUAL);
            paymentService.processPayment(donateInformation);
            pageTitle = "Donate Confirmation";
            message = "Your payment information was sent to your email. Please check your email.";
        } catch (InvalidMessageException | PayPalApiException | DataProcessNotFoundException e) {
            pageTitle = "Donate Failure";
            message = e.getMessage();
        }

        theModel.addAttribute("pageTitle", pageTitle);
        theModel.addAttribute("message", message);
        return "views/message";
    }
}
