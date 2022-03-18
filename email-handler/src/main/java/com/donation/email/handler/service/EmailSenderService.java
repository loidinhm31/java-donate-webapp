package com.donation.email.handler.service;

import com.donation.email.handler.enums.EmailTemplate;

import java.util.HashMap;
import java.util.List;

public interface EmailSenderService {
    String sendComposedEmail(EmailTemplate emailTemplate, List<String> toUsers, List<String > ccUsers,
                             String subject, HashMap<String, Object> dynamicData);

}
