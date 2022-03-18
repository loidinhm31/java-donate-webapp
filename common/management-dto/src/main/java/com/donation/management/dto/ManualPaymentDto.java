package com.donation.management.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentDto {
    private String projectId;

    private String transactionCode;

    private String transferCode;

    private Float amount;
}
