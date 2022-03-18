package com.donation.email.handler.service;

import com.donation.email.handler.model.EmailBean;

public interface MailServerManage {
    String sendMail(EmailBean emailBean);
}
