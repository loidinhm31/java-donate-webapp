package com.donation.email.handler.enums;

import static com.donation.email.handler.constant.EmailSubject.*;

public enum EmailTemplate {
    EMAIL_VERIFICATION(
      SUBJECT_AUTO_EMAIL_VERIFY,
      "user_verification_template.html"
    ),

    EMAIL_RESET_PASSWORD(
            SUBJECT_RESET_PASSWORD,
      "user_reset_password_template.html"
    ),

    EMAIL_REMIND_DONATE(
            SUBJECT_COMPLETE_DONATION,
            "remind_transfer_money_template.html"
    ),

    EMAIL_CONFIRM_DONATE(
            SUBJECT_CONFIRM_DONATION,
            "confirm_donate_template.html"
    );;

    public final String baseSubject;
    public final String templateFileName;

    EmailTemplate(String baseSubject, String templateFileName) {
        this.baseSubject = baseSubject;
        this.templateFileName = templateFileName;
    }
}
