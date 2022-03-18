package com.donation.management.model;

import com.donation.common.core.enums.PaymentMethodType;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonateInformation {

    private String supporterName;

    private String projectId;

    private String projectName;

    private Float donateAmount;

    private String messageDonate;

    private String email;

    private Boolean isDisplayName;

    private String transactionId;

    private PaymentMethodType methodType;

}
