package com.donation.data.handler.service.impl;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.Beneficiary;
import com.donation.common.management.mapper.BeneficiaryMapper;
import com.donation.common.management.repository.BeneficiaryRepository;
import com.donation.common.security.SecurityUtils;
import com.donation.data.handler.service.BeneficiaryService;
import com.donation.data.handler.service.GenerateIdService;
import com.donation.management.dto.BeneficiaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.donation.common.core.constant.MessageConstants.AUTHORIZE_EXCEPTION;

@Service
public class BeneficiaryServiceImpl extends BaseInitiatedServiceImpl
        implements BeneficiaryService {

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private GenerateIdService generateIdService;

    @Autowired
    private BeneficiaryMapper beneficiaryMapper;

    @Override
    public List<BeneficiaryDto> getBeneficiaries() {
        List<Beneficiary> beneficiaries = beneficiaryRepository.findAll();

        return beneficiaries.stream()
                .map(beneficiaryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Beneficiary getBeneficiary(String beneficiaryId) {
        Optional<Beneficiary> beneficiary = beneficiaryRepository.findById(beneficiaryId);
        return beneficiary.get();
    }

    @Override
    public void createBeneficiary(BeneficiaryDto beneficiaryDto) throws Exception {
        if (Objects.isNull(beneficiaryDto.getBeneficiaryName())) {
            throw new InvalidMessageException("Beneficiary Name invalid");
        }

        if (Objects.isNull(beneficiaryDto.getBeneficiaryPhoneNumber())) {
            throw new InvalidMessageException("Beneficiary Phone Number invalid");
        }

        if (checkPermissionWithRole()) {
            Beneficiary beneficiary = beneficiaryMapper.toEntity(beneficiaryDto);
            // Generate project id
            beneficiary.setBeneficiaryId(generateIdService.generateBeneficiaryId(beneficiary.getBeneficiaryName()));
            beneficiary.setCreatedBy(SecurityUtils.getUserIdentifier());
            beneficiary.setUpdatedBy(SecurityUtils.getUserIdentifier());
            beneficiary.setIsReceive(Boolean.FALSE);

            beneficiaryRepository.save(beneficiary);
        } else throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
    }

}
