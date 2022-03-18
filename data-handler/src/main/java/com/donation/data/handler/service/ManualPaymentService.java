package com.donation.data.handler.service;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.management.dto.ManualPaymentDto;
import org.springframework.web.multipart.MultipartFile;

public interface ManualPaymentService {
    Boolean updateTransferInformation(ManualPaymentDto manualPaymentDto) throws InvalidMessageException;

    void scanFile(MultipartFile multipartFile) throws InvalidMessageException;
}
