package com.donation.data.handler.service;

import com.donation.common.management.entity.DonationTime;
import com.donation.management.dto.DonationTimeDto;
import com.donation.management.dto.ProjectDto;
import com.donation.management.model.ProjectSearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DonationTimeService {
    Page<DonationTimeDto> getDonators(String projectId, Pageable pageable);

    DonationTime findDonationTime(String transferCode, String projectId);

    void updateTransfer(DonationTime donationTime);

    Page<DonationTimeDto> getDonationTimeByUser(int currentPage, int pageSize);

    DonationTime findDonationTimeByTransactionCode(String transactionId);

    void updateAll(List<DonationTime> donationTimes);
}
