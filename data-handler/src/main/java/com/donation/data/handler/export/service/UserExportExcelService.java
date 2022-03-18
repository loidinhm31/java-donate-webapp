package com.donation.data.handler.export.service;

import com.donation.data.handler.export.ExportDto;
import com.donation.management.model.UserFilter;
import org.springframework.core.io.ByteArrayResource;

public interface UserExportExcelService {
    ByteArrayResource exportExcelUserList(UserFilter userFilter, ExportDto exportDto) throws Exception;
}
