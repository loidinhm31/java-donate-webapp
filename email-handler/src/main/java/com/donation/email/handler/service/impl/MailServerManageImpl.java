package com.donation.email.handler.service.impl;

import com.donation.email.handler.model.EmailBean;
import com.donation.email.handler.model.FileUploadObject;
import com.donation.email.handler.service.MailServerManage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class MailServerManageImpl implements MailServerManage {

    @Value("${sender.mail.host}")
    private String mailHost;

    @Value("${sender.mail.port}")
    private String mailPort;

    @Value("${sender.mail.username}")
    private String mailUsername;

    @Value("${sender.mail.password}")
    private String mailPassword;

    @Override
    public String sendMail(EmailBean emailBean) {
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(mailHost);
            mailSender.setPort(Integer.parseInt(mailPort));
            mailSender.setUsername(mailUsername);
            mailSender.setPassword(mailPassword);

            // Set Auth properties
            Properties mailProperties = new Properties();
            mailProperties.setProperty("mail.smtp.auth", "true");
            mailProperties.setProperty("mail.smtp.starttls.enable", "true");

            mailSender.setJavaMailProperties(mailProperties);

            MimeMessage message = mailSender.createMimeMessage();
            message.setSubject(emailBean.getEmailSubject());
            message.setText(emailBean.getEmailBody(), "utf-8", "html");
            message.setFrom(new InternetAddress(emailBean.getFrom()));

            List<String> to = new ArrayList<>();
            if (Objects.nonNull(emailBean.getTo())) {
                to = getEmailList(emailBean.getTo());
            }
            if (to.size() != 0) {
                for (String str : to) {
                    message.addRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(str)));
                }
            }

            List<String> cc = new ArrayList<>();
            if (Objects.nonNull(emailBean.getCc())) {
                cc = getEmailList(emailBean.getCc());
            }
            if (cc.size() != 0) {
                for (String str : cc) {
                    message.addRecipients(Message.RecipientType.CC, String.valueOf(new InternetAddress(str)));
                }
            }

            MimeBodyPart messageBodyPart;
            if (Objects.nonNull(emailBean.getUploads()) && emailBean.getUploads().size() != 0) {
                Multipart multipart = new MimeMultipart();
                for (FileUploadObject file : emailBean.getUploads()) {
                    messageBodyPart = new MimeBodyPart();
                    File newFile = new File("\\mailAttachment\\" + file.getFileName());

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        fos.write(file.getFileData());
                        DataSource source = new FileDataSource(newFile);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(file.getFileName());
                        multipart.addBodyPart(messageBodyPart);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                BodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(emailBean.getEmailBody(), "text/html");
                multipart.addBodyPart(htmlPart);
                message.setContent(multipart);
            }
            mailSender.send(message);
            log.info("Email was sent successfully");
            return "success";
        } catch (MessagingException e) {
            log.error("Cannot send email", e);
            return "fail";
        }
    }

    private List<String> getEmailList(String str) {
        List<String> humans = new ArrayList<>();
        if (Objects.nonNull(str) && str.contains(",")) {
            String[] cced = str.split(",");
            humans = Arrays.asList(cced);
        } else {
            humans.add(str);
        }
        return humans;
    }

}
