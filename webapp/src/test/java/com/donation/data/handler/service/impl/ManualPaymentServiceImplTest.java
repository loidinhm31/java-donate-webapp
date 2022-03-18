package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.enums.TransactionStatus;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.*;
import com.donation.common.security.CustomUserDetail;
import com.donation.data.handler.service.DonationTimeService;
import com.donation.data.handler.service.ManualPaymentService;
import com.donation.data.handler.service.ProjectService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.impl.DonationTimeServiceImpl;
import com.donation.data.handler.service.impl.ManualPaymentServiceImpl;
import com.donation.data.handler.service.impl.ProjectServiceImpl;
import com.donation.data.handler.service.impl.UserAccountDetailsServiceImpl;
import com.donation.management.dto.ManualPaymentDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManualPaymentServiceImplTest {
    @InjectMocks
    private ManualPaymentService manualPaymentServiceTest = new ManualPaymentServiceImpl();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Mock
    private DonationTimeService donationTimeService = new DonationTimeServiceImpl();

    @Mock
    private ProjectService projectService = new ProjectServiceImpl();

    @Test
    public void testUpdateTransferInformation() throws InvalidMessageException {
        buildForAdmin();

        String projectId = "NC-2100001";
        String transferCode = "NC220119101212";
        ManualPaymentDto manualPaymentDto = ManualPaymentDto.builder()
                .projectId(projectId)
                .transferCode(transferCode)
                .transactionCode("XYZ10101010")
                .amount(200000F)
                .build();

        Project project = Project.builder()
                .projectId(projectId)
                .status(ProjectStatusEnum.IN_PROGRESS)
                .currentMoney(0F)
                .targetMoney(1000000F)
                .build();

        when(projectService.findProjectByProjectIdAndStatus(projectId, ProjectStatusEnum.IN_PROGRESS))
                .thenReturn(project);


        DonationTime donationTime = DonationTime.builder()
                .id(transferCode)
                .paymentMethod(PaymentMethodType.MANUAL)
                .build();
        when(donationTimeService.findDonationTime(manualPaymentDto.getTransferCode(), manualPaymentDto.getProjectId()))
                .thenReturn(donationTime);

        // Call the function to test
        Boolean isComplete = manualPaymentServiceTest.updateTransferInformation(manualPaymentDto);

        assertThat(isComplete).isTrue();
        assertThat(project.getCurrentMoney())
                .isEqualTo(200000F);
    }

    @Test
    public void testScanFile() throws InvalidMessageException, ParseException {
        buildForAdmin();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        MultipartFile multipartFile = null;
        try (Workbook workbook = new SXSSFWorkbook();
             Workbook mockWorkbook = spy(workbook);
             ByteArrayOutputStream bao = new ByteArrayOutputStream()) {

            Sheet mockSheet = mockWorkbook.createSheet("sheet1");

            // Build header
            String[] headers = {"Time", "Description", "Amount", "Transaction Code"};
            Row mockRow = mockSheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell mockCell = mockRow.createCell(i);
                mockCell.setCellValue(headers[i]);
            }

            // Build content 1
            Row mockRowContent1 = mockSheet.createRow(1);
            mockRowContent1.createCell(0)
                    .setCellValue(new Date());

            mockRowContent1.createCell(1)
                    .setCellValue("NC-21000001 NC210405103033");

            mockRowContent1.createCell(2)
                    .setCellValue("500000");

            mockRowContent1.createCell(3)
                    .setCellValue("XZP63AWW8FJKP5IH8");

            // Build content 2
            Row mockRowContent2 = mockSheet.createRow(2);
            mockRowContent2.createCell(0)
                    .setCellValue(simpleDateFormat.parse("30/12/2021"));

            mockRowContent2.createCell(1)
                    .setCellValue("NC-21000002 NC210408103034");

            mockRowContent2.createCell(2)
                    .setCellValue("500000");

            mockRowContent2.createCell(3)
                    .setCellValue("IWF53SUP7KSEM6SO6");

            mockWorkbook.write(bao);

            multipartFile = new MockMultipartFile("file", "orig.xlsx", "application/vnd.ms-excel", bao.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Project projectOne = Project.builder()
                .projectId("NC-21000001")
                .status(ProjectStatusEnum.IN_PROGRESS)
                .currentMoney(0F)
                .targetMoney(1000000F)
                .build();

        Project projectTwo = Project.builder()
                .projectId("NC-21000002")
                .status(ProjectStatusEnum.CLOSED)
                .currentMoney(0F)
                .targetMoney(1000000F)
                .updatedAt(simpleDateFormat.parse("01/01/2022"))
                .build();

        DonationTime donationTimeOne = DonationTime.builder()
                .id("NC210405103033")
                .paymentMethod(PaymentMethodType.MANUAL)
                .status(TransactionStatus.PENDING)
                .project(projectOne)
                .build();

        DonationTime donationTimeTwo = DonationTime.builder()
                .id("NC210408103034")
                .paymentMethod(PaymentMethodType.MANUAL)
                .status(TransactionStatus.PENDING)
                .project(projectTwo)
                .build();


        when(donationTimeService.findDonationTime("NC210405103033", "NC-21000001"))
                .thenReturn(donationTimeOne);

        when(donationTimeService.findDonationTime("NC210408103034", "NC-21000002"))
                .thenReturn(donationTimeTwo);

        // Call the function to test
        manualPaymentServiceTest.scanFile(multipartFile);

        assertThat(projectOne.getCurrentMoney())
                .isEqualTo(500000F);

        assertThat(projectTwo.getCurrentMoney())
                .isEqualTo(500000F);
    }

    private UserAccountDetail buildForAdmin() {
        // Custom authentication
        String userIdentifier = "root";
        UUID userId = UUID.randomUUID();

        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .username(userIdentifier)
                .userPassword("root")
                .userRoles(List.of(UserRole.builder()
                        .role(Role.builder()
                                .roleId(UUID.randomUUID())
                                .roleCode(RoleEnum.ADMIN)
                                .roleName(RoleEnum.ADMIN.getRoleName())
                                .build())
                        .build()))
                .build();

        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(userId)
                .userAccount(userAccount)
                .build();
        userAccount.setUserAccountDetail(userAccountDetail);

        CustomUserDetail customUserDetail =
                new CustomUserDetail(userAccount);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(customUserDetail);

        when(userAccountDetailsService.findUserByIdentifier(userIdentifier))
                .thenReturn(userAccountDetail);

        return userAccountDetail;
    }
}
