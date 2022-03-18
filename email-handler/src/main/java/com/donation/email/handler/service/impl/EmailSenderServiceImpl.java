package com.donation.email.handler.service.impl;

import com.donation.email.handler.enums.EmailTemplate;
import com.donation.email.handler.model.EmailBean;
import com.donation.email.handler.service.EmailSenderService;
import com.donation.email.handler.service.MailServerManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailSenderServiceImpl implements EmailSenderService {

    @Value("${sender.mail.from}")
    private String fromEmail;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private MailServerManage mailServerManage;

    @Override
    public String sendComposedEmail(EmailTemplate emailTemplate, List<String> toUsers, List<String > ccUsers,
                                    String subject, HashMap<String, Object> dynamicData) {
        log.info("Send Composed email");
        EmailBean emailBean = buildComposedEmail(emailTemplate, toUsers, ccUsers, subject, dynamicData);

        return mailServerManage.sendMail(emailBean);
    }

    private EmailBean buildComposedEmail(EmailTemplate emailTemplate, List<String> toUsers, List<String> ccUsers,
                                         String subject, HashMap<String, Object> dynamicData) {
        log.info("To: {}", toUsers.toString());
        log.info("Cc: {}", ccUsers.toString());

        toUsers = toUsers.stream().map(String::toLowerCase).distinct().collect(Collectors.toList());
        ccUsers = ccUsers.stream().map(String::toLowerCase).distinct().collect(Collectors.toList());

        return EmailBean.builder()
                .from(fromEmail)
                .to(String.join(",", toUsers))
                .cc(CollectionUtils.isEmpty(ccUsers) ? null : String.join(",", ccUsers))
                .emailSubject(subject)
                .emailBody(buildHTMLEmailBodyByEmailTemplateType(emailTemplate, dynamicData))
                .build();
    }

    private String buildHTMLEmailBodyByEmailTemplateType(EmailTemplate emailTemplate, HashMap<String, Object> dynamicData) {
        Context context = new Context(null, dynamicData);

        // Build body
        return springTemplateEngine.process(emailTemplate.templateFileName, context);

    }


}
