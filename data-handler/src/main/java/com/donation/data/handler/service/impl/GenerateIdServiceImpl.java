package com.donation.data.handler.service.impl;

import com.donation.common.management.entity.BeneficiaryIncrementId;
import com.donation.common.management.entity.ProjectIncrementId;
import com.donation.common.management.repository.BeneficiaryIncrementIdRepository;
import com.donation.common.management.repository.ProjectIncrementIdRepository;
import com.donation.data.handler.service.GenerateIdService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class GenerateIdServiceImpl implements GenerateIdService {
    @Autowired
    private ProjectIncrementIdRepository projectIncrementIdRepository;

    @Autowired
    private BeneficiaryIncrementIdRepository beneficiaryIncrementIdRepository;

    @Override
    public String generateProjectId(String projectName) throws Exception {
        ProjectIncrementId projectIncrementId = new ProjectIncrementId();
        ProjectIncrementId savedProjectIncrementId = projectIncrementIdRepository.save(projectIncrementId);
        Long generatedId = savedProjectIncrementId.getProjectId();
        if (Objects.nonNull(generatedId)) {
            return generateFirstPrefixId() + "-" + getSecondPrefixId() + String.format("%06d", generatedId);
        } else {
            throw new Exception();
        }
    }

    @Override
    public String generateBeneficiaryId(String beneficiaryName) throws Exception {
        BeneficiaryIncrementId beneficiaryIncrementId = new BeneficiaryIncrementId();
        BeneficiaryIncrementId savedBeneficiaryIncrementId = beneficiaryIncrementIdRepository.save(beneficiaryIncrementId);

        Long generatedId = savedBeneficiaryIncrementId.getBeneficiaryId();
        if (Objects.nonNull(generatedId)) {
            return generateFirstPrefixId() + "-" + getSecondPrefixId() + String.format("%06d", generatedId);
        } else {
            throw new Exception();
        }
    }

    @Override
    public String generateTransferId(String projectId) {
        Calendar cal = Calendar.getInstance();
        String prefix = projectId.split("-")[0];

        String month;
        if (cal.get(Calendar.MONTH) < 10) {
            month = "0" + (cal.get(Calendar.MONTH) + 1);
        } else {
            month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        }

        String day;
        if (cal.get(Calendar.DAY_OF_MONTH) < 10) {
            day = "0" + cal.get(Calendar.DAY_OF_MONTH);
        } else {
            day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        }

        String time;
        if (cal.get(Calendar.HOUR_OF_DAY) < 10) {
            time = "0" + cal.get(Calendar.HOUR_OF_DAY);
        } else {
            time = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        }

        String minute;
        if (cal.get(Calendar.MINUTE) < 10) {
            minute = "0" + cal.get(Calendar.MINUTE);
        } else {
            minute = String.valueOf(cal.get(Calendar.MINUTE));
        }

        String second;
        if (cal.get(Calendar.SECOND) < 10) {
            second = "0" + cal.get(Calendar.SECOND);
        } else {
            second = String.valueOf(cal.get(Calendar.SECOND));
        }

        return prefix + getSecondPrefixId() +
                month +
                day +
                time +
                minute +
                second;
        }

    public String generateFirstPrefixId() {
        Calendar cal = Calendar.getInstance();
        String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

        monthName = monthName.substring(0, 3).toUpperCase();

        return monthName;
    }

    private String getSecondPrefixId() {
        String strYear = String.valueOf(LocalDate.now().getYear());
        String secondPrefix;
        if (strYear.length() <= 2) {
            secondPrefix = strYear;
        } else {
            secondPrefix = strYear.substring(strYear.length() - 2);
        }
        return secondPrefix;
    }
}
