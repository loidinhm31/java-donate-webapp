package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.TransactionStatus;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.DonationTime;
import com.donation.common.management.entity.DonationTime_;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.mapper.DonationTimeMapper;
import com.donation.common.management.repository.DonationTimeRepository;
import com.donation.common.security.CustomUserDetail;
import com.donation.common.security.SecurityUtils;
import com.donation.data.handler.service.DonationTimeService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.management.dto.DonationTimeDto;
import com.donation.management.dto.ProjectDto;
import com.donation.management.model.ProjectSearchFilter;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationTimeServiceImpl implements DonationTimeService {
    @Autowired
    private DonationTimeRepository donationTimeRepository;

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private DonationTimeMapper donationTimeMapper;

    @Override
    public Page<DonationTimeDto> getDonators(String projectId, Pageable pageable) {
        Page<DonationTime> donationTimeList =
                donationTimeRepository.findDonationTimeByProjectAndStatus(projectId, TransactionStatus.COMPLETE.name(), pageable);

        return donationTimeList.map(donationTimeMapper::donationTimeToDonator);
    }

    @Override
    public DonationTime findDonationTime(String transferCode, String projectId) {
        DonationTime donationTime = donationTimeRepository.findByIdAndProjectId(transferCode, projectId);

        return donationTime;
    }

    @Override
    public void updateTransfer(DonationTime donationTime) {
        donationTimeRepository.save(donationTime);
    }

    @Override
    public Page<DonationTimeDto> getDonationTimeByUser(int currentPage, int pageSize) {

        UserAccountDetail userAccountDetail =
                userAccountDetailsService.findUserByIdentifier(SecurityUtils.getUserIdentifier());

        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.DESC, "created_at");

        Page<DonationTime> donationTimeList =
                donationTimeRepository.findDonationTimeByUser(userAccountDetail.getUserId(), pageable);

        return donationTimeList.map(donationTimeMapper::donationTimeToDonatedProject);
    }

    @Override
    public DonationTime findDonationTimeByTransactionCode(String transactionId) {
        DonationTime donationTime = donationTimeRepository.findByTransactionCode(transactionId);
        return donationTime;
    }

    @Override
    public void updateAll(List<DonationTime> donationTimes) {
        donationTimeRepository.saveAll(donationTimes);
    }
}
