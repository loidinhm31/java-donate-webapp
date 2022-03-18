package com.donation.common.management.mapper;

import com.donation.common.management.entity.Beneficiary;
import com.donation.management.dto.BeneficiaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BeneficiaryMapper {
    Beneficiary toEntity(BeneficiaryDto beneficiary);

    BeneficiaryDto toDto(Beneficiary beneficiary);
}
