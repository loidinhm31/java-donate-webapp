package com.donation.email.handler.service.impl;

import com.donation.email.handler.model.EmailBusinessDto;
import com.donation.email.handler.enums.EmailTemplate;
import com.donation.email.handler.service.EmailDataHandlerService;
import com.donation.email.handler.service.EmailSenderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.donation.common.core.constant.EmailDynamicDataKey.*;

@Slf4j
@Service
public class EmailDataHandlerServiceImpl implements EmailDataHandlerService {

    @Value("${sender.mail.active}")
    private Boolean emailActive;

    @Value("${sender.mail.from}")
    private String mailFrom;

    @Autowired
    private EmailSenderService emailSenderService;

    @Override
    public void handleDataAndSendMail(EmailBusinessDto emailBusinessDto, EmailTemplate emailTemplate, List<String> toUsers, List<String> ccUsers, String subject) {
        if (emailActive) {
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("Sending email...");
                    emailBusinessDto.setSupportService(mailFrom);

                    // Generate Subject
                    String generatedSubject = generateSubject(subject);

                    // Generate Greeting
                    String greeting = generateGreeting(emailBusinessDto);

                    // Generate dynamic data
                    HashMap<String, Object> dynamicData = generateDynamicData(emailBusinessDto, greeting);

                    // Send email
                    String response = emailSenderService.sendComposedEmail(emailTemplate, toUsers, ccUsers, generatedSubject, dynamicData);

                } catch (RuntimeException e) {
                    log.error("Send email exception: {}", e.getMessage(), e);
                }
            });
        }
    }

    private String generateSubject(String subjectTemplate) {
        return subjectTemplate.replace("@E_SITE", "DONATION");
    }

    private String generateGreeting(EmailBusinessDto emailBusinessDto) {
        String greeting = StringUtils.EMPTY;
        if (Objects.nonNull(emailBusinessDto.getFirstName()) && Objects.nonNull(emailBusinessDto.getLastName())) {
            greeting = emailBusinessDto.getLastName() + StringUtils.SPACE + emailBusinessDto.getFirstName();
        } else if (Objects.nonNull(emailBusinessDto.getUserName())) {
            greeting = emailBusinessDto.getUserName();
        }
        return greeting;
    }

    private HashMap<String, Object> generateDynamicData(EmailBusinessDto emailBusinessDto, String greeting) {
        HashMap<String, Object> dynamicData = new HashMap<>();

        dynamicData.put(E_GREETING, greeting);
        dynamicData.put(E_USERNAME, Objects.nonNull(emailBusinessDto.getUserName()) ? emailBusinessDto.getUserName() : null);
        dynamicData.put(E_SUPPORT, "DONATION SUPPORT TEAM");
        dynamicData.put(E_SUPPORT_LINK, Objects.nonNull(emailBusinessDto.getSupportService()) ? emailBusinessDto.getSupportService() : null);

        switch (emailBusinessDto.getMailType()) {
            case USER:
                dynamicData.put(E_FIRST_NAME, Objects.nonNull(emailBusinessDto.getFirstName()) ? emailBusinessDto.getFirstName() : null);
                dynamicData.put(E_LAST_NAME, Objects.nonNull(emailBusinessDto.getLastName()) ? emailBusinessDto.getLastName() : null);
                dynamicData.put(E_PHONE, Objects.nonNull(emailBusinessDto.getPhone()) ? emailBusinessDto.getPhone() : null);
                dynamicData.put(E_LINK, Objects.nonNull(emailBusinessDto.getLink()) ? emailBusinessDto.getLink() : null);
                break;
            case DONATE:
                dynamicData.put(E_PROJECT_ID, Objects.nonNull(emailBusinessDto.getProjectId()) ? emailBusinessDto.getProjectId() : null);
                dynamicData.put(E_PROJECT_NAME, Objects.nonNull(emailBusinessDto.getProjectName()) ? emailBusinessDto.getProjectName() : null);
                dynamicData.put(E_AMOUNT, Objects.nonNull(emailBusinessDto.getAmount()) ? emailBusinessDto.getAmount() : null);
                dynamicData.put(E_CURRENCY, Objects.nonNull(emailBusinessDto.getCurrency()) ? emailBusinessDto.getCurrency() : null);
                dynamicData.put(E_TRANSFER_CODE, Objects.nonNull(emailBusinessDto.getTransferCode()) ? emailBusinessDto.getTransferCode() : null);
                break;
        }

        return dynamicData;
    }
}
