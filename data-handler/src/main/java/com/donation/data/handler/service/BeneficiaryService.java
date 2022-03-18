package com.donation.data.handler.service;


import com.donation.common.management.entity.Beneficiary;
import com.donation.management.dto.BeneficiaryDto;

import java.util.List;

public interface BeneficiaryService {
    List<BeneficiaryDto> getBeneficiaries();

    Beneficiary getBeneficiary(String beneficiaryId);

    void createBeneficiary(BeneficiaryDto beneficiaryDto) throws Exception;
}
