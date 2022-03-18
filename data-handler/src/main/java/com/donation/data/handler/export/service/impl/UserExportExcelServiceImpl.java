package com.donation.data.handler.export.service.impl;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.repository.UserAccountDetailRepository;
import com.donation.common.management.repository.projection.UserAccountProjection;
import com.donation.data.handler.export.ExportDto;
import com.donation.data.handler.export.ExportEnum;
import com.donation.data.handler.export.service.UserExportExcelService;
import com.donation.data.handler.service.impl.BaseInitiatedServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.donation.management.model.UserFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.donation.common.core.constant.MessageConstants.AUTHORIZE_EXCEPTION;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER;


@Slf4j
@Service
public class UserExportExcelServiceImpl extends BaseInitiatedServiceImpl
        implements UserExportExcelService {
    public static final String EXCEL_FORMAT_DATE = "MM/dd/yyyy";
    private XSSFCellStyle formatDateCellStyle;

    @Autowired
    private UserAccountDetailRepository userAccountDetailRepository;

    @Override
    public ByteArrayResource exportExcelUserList(UserFilter userFilter, ExportDto exportDto) throws Exception {
        if (!checkPermissionWithRole()) {
            throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
        }

        List<UserAccountProjection> userAccountDetails =
                userAccountDetailRepository.searchUsers(userFilter).getContent();

        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            log.info("Start writing excel...");
            XSSFSheet xssfSheet = initExcelFile(xssfWorkbook, "Sheet 1", exportDto);

            fillData(xssfSheet, userAccountDetails, exportDto.getColumns());

            xssfWorkbook.write(byteArrayOutputStream);
            log.info("Export success: {}", exportDto.getFileName());
            return new ByteArrayResource(byteArrayOutputStream.toByteArray());
        } catch (RuntimeException | IOException e) {
            log.error("Export error cause: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private XSSFSheet initExcelFile(XSSFWorkbook xssfWorkbook, String sheetName, ExportDto dto) {
        XSSFSheet xssfSheet = xssfWorkbook.createSheet(sheetName);
        XSSFCellStyle formatHeaderCellStyle = formatHeaderCellStyle(xssfWorkbook);
        formatDateCellStyle = formatDateCellStyle(xssfWorkbook);

        Row headerRow = xssfSheet.createRow(0);
        for (int index = 0; index < dto.getColumns().size(); index++) {
            Cell cell = headerRow.createCell(index);
            cell.setCellStyle(formatHeaderCellStyle);
            cell.setCellValue(dto.getColumns().get(index).getHeader());
            xssfSheet.autoSizeColumn(index);
        }
        return xssfSheet;
    }

    private static XSSFCellStyle formatHeaderCellStyle(XSSFWorkbook xssfWorkbook) {
        XSSFFont xssfFont = xssfWorkbook.createFont();
        xssfFont.setBold(true);
        XSSFCellStyle xssfCellStyle = xssfWorkbook.createCellStyle();
        xssfCellStyle.setFont(xssfFont);
        xssfCellStyle.setAlignment(CENTER);
        return xssfCellStyle;
    }

    private static XSSFCellStyle formatDateCellStyle(XSSFWorkbook xssfWorkbook) {
        XSSFCellStyle xssfCellStyle = xssfWorkbook.createCellStyle();
        CreationHelper creationHelper = xssfWorkbook.getCreationHelper();
        xssfCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(EXCEL_FORMAT_DATE));
        xssfCellStyle.setAlignment(CENTER);
        return xssfCellStyle;
    }

    private void fillData(XSSFSheet xssfSheet,
                          List<UserAccountProjection> userList,
                          List<ExportEnum> columns) {
        final int[] rowCount = {1};
        for (UserAccountProjection user : userList) {
            try {
                Row row = xssfSheet.createRow(rowCount[0]++);
                fillDataRow(row, user, columns);
            } catch (RuntimeException e) {
                log.error(String.format("Error excel at: ID %s", user), e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void fillDataRow(Row row, UserAccountProjection record, List<ExportEnum> columns) {
        columns.forEach(col -> {
            Cell rowCell = row.createCell(columns.indexOf(col));
            switch (col) {
                case USERNAME:
                    rowCell.setCellValue(Objects.nonNull(record.getUsername()) ? record.getUsername() : StringUtils.EMPTY);
                    break;
                case EMAIL:
                    rowCell.setCellValue(Objects.nonNull(record.getEmail()) ? record.getEmail() : StringUtils.EMPTY);
                    break;
                case FIRST_NAME:
                    rowCell.setCellValue(Objects.nonNull(record.getFirstName()) ? record.getFirstName() : StringUtils.EMPTY);
                    break;
                case LAST_NAME:
                    rowCell.setCellValue(Objects.nonNull(record.getLastName()) ? record.getLastName() : StringUtils.EMPTY);
                    break;
                case PHONE_NUMBER:
                    rowCell.setCellValue(Objects.nonNull(record.getPhoneNumber()) ? record.getPhoneNumber() : StringUtils.EMPTY);
                    break;
                case DATE_OF_BIRTH:
                    rowCell.setCellValue(Objects.nonNull(record.getBirthdate()) ? record.getBirthdate().toString(): StringUtils.EMPTY);
                    break;
                case LAST_LOGIN_TIME:
                    rowCell.setCellValue(Objects.nonNull(record.getLastedLoginTime()) ? record.getLastedLoginTime().toString() : StringUtils.EMPTY);
                    break;
                case VERIFIED:
                    rowCell.setCellValue((Objects.nonNull(record.getIsVerify()) && record.getIsVerify()) ? "Yes" : "No");
                    break;
                case ACTIVE_OR_DEACTIVATE:
                    rowCell.setCellValue((Objects.nonNull(record.getIsActive()) && record.getIsActive()) ? "Active" : "Deactivate");
                    break;
                default:
                    rowCell.setCellValue(StringUtils.EMPTY);
            }
        });
    }


}
