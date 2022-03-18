package com.donation.email.handler.service;

import com.donation.email.handler.model.EmailBusinessDto;
import com.donation.email.handler.enums.EmailTemplate;

import java.util.List;

public interface EmailDataHandlerService {
   void handleDataAndSendMail(EmailBusinessDto emailBusinessDto, EmailTemplate emailTemplate, List<String> toUsers, List<String> ccUsers, String subject);
}
