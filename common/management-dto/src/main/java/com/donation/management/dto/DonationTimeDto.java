package com.donation.management.dto;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DonationTimeDto extends BaseDto {

    private String id;

    private String supporterName;

    private Boolean isDisplayName;

    private String message;

    private String transactionCode;

    private PaymentMethodType paymentMethod;

    private Float amount;

    private TransactionStatus status;

    private ProjectDto project;
}
