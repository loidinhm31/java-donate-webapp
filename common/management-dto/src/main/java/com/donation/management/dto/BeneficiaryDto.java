package com.donation.management.dto;

import com.donation.common.core.enums.BeneficiaryType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeneficiaryDto {
    private String beneficiaryId;

    private String beneficiaryName;

    private String beneficiaryPhoneNumber;

    private BeneficiaryType beneficiaryType;
}
