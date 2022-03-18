package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.enums.TransactionStatus;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.DonationTime;
import com.donation.common.management.entity.Project;
import com.donation.common.security.SecurityUtils;
import com.donation.data.handler.service.DonationTimeService;
import com.donation.data.handler.service.GenerateIdService;
import com.donation.data.handler.service.ManualPaymentService;
import com.donation.data.handler.service.ProjectService;
import com.donation.management.dto.ManualPaymentDto;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.donation.common.core.constant.MessageConstants.AUTHORIZE_EXCEPTION;
import static com.donation.common.core.constant.MessageConstants.PROJECT_STATUS_EXCEPTION;

@Slf4j
@Service
public class ManualPaymentServiceImpl extends BaseInitiatedServiceImpl
        implements ManualPaymentService {

    @Autowired
    private DonationTimeService donationTimeService;

    @Autowired
    private ProjectService projectService;

    @Override
    public Boolean updateTransferInformation(ManualPaymentDto manualPaymentDto)
            throws InvalidMessageException {

        // Check permission
        if (!checkPermissionWithRole()) {
            throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
        }

        if (Objects.isNull(manualPaymentDto.getProjectId())) {
            throw new InvalidMessageException("Invalid Project Code");
        }

        if (Objects.isNull(manualPaymentDto.getTransactionCode())) {
            throw new InvalidMessageException("Invalid Transaction Code");
        }

        if (Objects.isNull(manualPaymentDto.getAmount())) {
            throw new InvalidMessageException("Invalid Amount");
        }

        // Get project
        Project project =
                projectService.findProjectByProjectIdAndStatus(manualPaymentDto.getProjectId(),
                        ProjectStatusEnum.IN_PROGRESS);
        if (Objects.isNull(project)) {
            throw new InvalidMessageException("Cannot find project or this project is not active");
        }

        DonationTime donationTime = null;
        if (Objects.nonNull(manualPaymentDto.getTransferCode())) {
            donationTime =
                    donationTimeService.findDonationTime(manualPaymentDto.getTransferCode(),
                            manualPaymentDto.getProjectId());
        }

        if (Objects.nonNull(donationTime)) {    // Process pending transfer
            if (Objects.isNull(donationTime.getTransactionCode())
                    && donationTime.getPaymentMethod().equals(PaymentMethodType.MANUAL)) {

                donationTime.setAmount(manualPaymentDto.getAmount());
                donationTime.setTransactionCode(manualPaymentDto.getTransactionCode().toUpperCase());
                // Change status
                donationTime.setStatus(TransactionStatus.COMPLETE);
                donationTime.setUpdatedBy(SecurityUtils.getUserIdentifier());

                try {
                    donationTimeService.updateTransfer(donationTime);
                } catch (DataIntegrityViolationException e) {
                    throw new InvalidMessageException("This transfer is duplicated");
                }

                // Update money for project
                updateTotalMoneyForProject(project, manualPaymentDto.getAmount());

                return Boolean.TRUE;

            } else throw new InvalidMessageException("This transfer was recognized");
        }
        return Boolean.FALSE;
    }

    @Override
    public void scanFile(MultipartFile multipartFile) throws InvalidMessageException {

        if (checkPermissionWithRole()) {
            try (InputStream inputStream = multipartFile.getInputStream();
                 Workbook workbook = StreamingReader.builder()
                         .rowCacheSize(10)
                         .bufferSize(1024)
                         .open(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);

                log.info("Scan bank statement file...");
                scanFile(sheet);
                log.info("...End Scan bank statement file");

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
    }

    private void scanFile(Sheet sheet) {
        List<DonationTime> objectList = new ArrayList<>();
        for (Row row : sheet) {
            DonationTime donationTime = null;
            Date donatedDate = null;
            for (Cell cell : row) {
                if (row.getRowNum() != 0) {
                    switch (cell.getColumnIndex()) {
                        case 0: // Transaction Date
                            donatedDate = cell.getDateCellValue();
                            break;
                        case 1: // Description
                            String[] content = cell.getStringCellValue().split(StringUtils.SPACE);
                            String projectId = content[0];
                            String transferId = content[1];

                            donationTime = donationTimeService.findDonationTime(transferId, projectId);
                            if (Objects.isNull(donationTime)) {
                                break;
                            } else if (donationTime.getStatus().equals(TransactionStatus.COMPLETE)) {
                                donationTime = null;
                                break;
                            }

                            if (donationTime.getProject().getStatus().equals(ProjectStatusEnum.IN_PROGRESS)
                                    || donationTime.getProject().getStatus().equals(ProjectStatusEnum.ENDED) ) {
                                // Check donate date before close date of project
                                if (Objects.nonNull(donatedDate)
                                        && donationTime.getProject().getStatus().equals(ProjectStatusEnum.CLOSED)
                                        && donatedDate.after(donationTime.getProject().getUpdatedAt())) {
                                    donationTime = null;
                                    break;
                                }
                            }
                            donationTime.setStatus(TransactionStatus.COMPLETE);

                            break;
                        case 2: // Amount
                            if (Objects.nonNull(donationTime)) {
                                donationTime.setAmount(Float.parseFloat(cell.getStringCellValue()));
                            }
                            break;
                        case 3: // Transaction code
                            if (Objects.nonNull(donationTime)) {
                                donationTime.setTransactionCode(cell.getStringCellValue());
                            }
                            break;
                    }
                }
            }
            if (Objects.nonNull(donationTime)) {
                log.info("Donate with transfer id {} was added to update...",  donationTime.getId());
                objectList.add(donationTime);

                // Add total for project
                updateTotalMoneyForProject(donationTime.getProject(), donationTime.getAmount());
            }
        }

        if (CollectionUtils.isNotEmpty(objectList)) {
            donationTimeService.updateAll(objectList);
        }
    }
}
