package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.core.exception.PayPalApiException;
import com.donation.common.management.entity.*;
import com.donation.common.security.CustomUserDetail;
import com.donation.data.handler.service.*;
import com.donation.data.handler.service.impl.*;
import com.donation.email.handler.service.EmailDataHandlerService;
import com.donation.email.handler.service.impl.EmailDataHandlerServiceImpl;
import com.donation.management.model.AmountResponse;
import com.donation.management.model.DonateInformation;
import com.donation.management.model.TransactionResponse;
import com.donation.payment.gateway.service.PaymentGateWayDistributeService;
import com.donation.payment.gateway.service.impl.PaymentGateWayDistributeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {
    @InjectMocks
    private PaymentService paymentServiceTest = new PaymentServiceImpl();

    @Mock
    private ProjectService projectService = new ProjectServiceImpl();

    @Mock
    private GenerateIdService generateIdService = new GenerateIdServiceImpl();

    @Mock
    private EmailDataHandlerService emailDataHandlerService = new EmailDataHandlerServiceImpl();

    @Mock
    private DonationTimeService donationTimeService = new DonationTimeServiceImpl();

    @Mock
    private PaymentGateWayDistributeService paymentGateWayDistributeService = new PaymentGateWayDistributeServiceImpl();

    @Mock
    private RestTemplate restTemplate = new RestTemplate();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Test
    public void testCheckProjectExist() throws InvalidMessageException {
        String projectId = "NC-2100001";
        DonateInformation donateInformation = DonateInformation.builder()
                .projectId(projectId)
                .build();

        Project project = Project.builder()
                .projectId(projectId)
                .projectName("TEST PROJECT")
                .build();

        when(projectService.findProject(donateInformation.getProjectId()))
                .thenReturn(project);

        // Call the function to test
        DonateInformation responseDonateInformation =
                paymentServiceTest.checkProjectExist(donateInformation);

        assertThat(responseDonateInformation.getProjectName()).isEqualTo("TEST PROJECT");
    }

    @Test
    public void testProcessPaymentWithManualMethod()
            throws InvalidMessageException, PayPalApiException, DataProcessNotFoundException {
        UserAccountDetail userAccountDetail = buildForAdmin();

        String projectId = "NC-2100001";
        DonateInformation donateInformation = DonateInformation.builder()
                .projectId(projectId)
                .donateAmount(300000F)
                .supporterName("A SUPPORTER")
                .email("supporter@gmail.com")
                .build();

        when(userAccountDetailsService.findUserByIdentifier("root"))
                .thenReturn(userAccountDetail);

        Project project = Project.builder()
                .projectId(projectId)
                .status(ProjectStatusEnum.IN_PROGRESS)
                .currentMoney(100000F)
                .targetMoney(1000000F)
                .build();

        when(projectService.findProject(projectId))
                .thenReturn(project);

        paymentServiceTest.processPayment(donateInformation);

        assertThat(project.getCurrentMoney()).isEqualTo(400000F);
    }

    @Test
    public void testProcessPaymentThroughGateway()
            throws InvalidMessageException, PayPalApiException, DataProcessNotFoundException {
        UserAccountDetail userAccountDetail = buildForAdmin();

        String projectId = "NC-2100001";
        DonateInformation donateInformation = DonateInformation.builder()
                .projectId(projectId)
                .donateAmount(20F)
                .supporterName("A SUPPORTER")
                .email("supporter@gmail.com")
                .methodType(PaymentMethodType.PAYPAL)
                .transactionId("GA123456")
                .build();

        when(userAccountDetailsService.findUserByIdentifier("root"))
                .thenReturn(userAccountDetail);

        Project project = Project.builder()
                .projectId(projectId)
                .status(ProjectStatusEnum.IN_PROGRESS)
                .currentMoney(100000F)
                .targetMoney(1000000F)
                .build();

        when(projectService.findProject(projectId))
                .thenReturn(project);

        when(generateIdService.generateTransferId(project.getProjectId()))
                .thenReturn("CA220128150315");

        TransactionResponse transactionResponse = TransactionResponse.builder()
                .amount(AmountResponse.builder()
                        .currencyCode("USD")
                        .value("30")
                        .build())
                .status("COMPLETED")
                .build();

        String response = "({\"items\":[{\"type\":\"GBP\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/GBP.gif\",\"muatienmat\":\"29980\",\"muack\":\"30110\",\"bantienmat\":\"30500\",\"banck\":\"30490\"},{\"type\":\"AUD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/AUD.gif\",\"muatienmat\":\"15690\",\"muack\":\"15780\",\"bantienmat\":\"16000\",\"banck\":\"15990\"},{\"type\":\"CAD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/CAD.gif\",\"muatienmat\":\"17550\",\"muack\":\"17650\",\"bantienmat\":\"17880\",\"banck\":\"17870\"},{\"type\":\"CHF\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/CHF.gif\",\"muatienmat\":\"22610\",\"muack\":\"24120\",\"bantienmat\":\"23080\",\"banck\":\"24450\"},{\"type\":\"CNY\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/CNY.gif\",\"muatienmat\":\"3000\",\"muack\":\"3000\",\"bantienmat\":\"3500\",\"banck\":\"3500\"},{\"type\":\"EUR\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/EUR.gif\",\"muatienmat\":\"24940\",\"muack\":\"25050\",\"bantienmat\":\"25370\",\"banck\":\"25360\"},{\"type\":\"XAU\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/XAU.gif\",\"muatienmat\":\"6185000\",\"muack\":\"6220000\",\"bantienmat\":\"6255000\",\"banck\":\"6220000\"},{\"type\":\"HKD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/HKD.gif\",\"muatienmat\":\"2410\",\"muack\":\"2890\",\"bantienmat\":\"2920\",\"banck\":\"2930\"},{\"type\":\"USD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/USD.gif\",\"muatienmat\":\"22560\",\"muack\":\"22560\",\"bantienmat\":\"22740\",\"banck\":\"22740\"},{\"type\":\"THB\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/THB.gif\",\"muatienmat\":\"610\",\"muack\":\"670\",\"bantienmat\":\"680\",\"banck\":\"680\"},{\"type\":\"SGD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/SGD.gif\",\"muatienmat\":\"16440\",\"muack\":\"16590\",\"bantienmat\":\"16810\",\"banck\":\"16810\"},{\"type\":\"PNJ_DAB\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/PNJ_DAB.gif\",\"muatienmat\":\"5260000\",\"muack\":\"5260000\",\"bantienmat\":\"5340000\",\"banck\":\"5340000\"},{\"type\":\"NZD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/NZD.gif\",\"muatienmat\":\"\",\"muack\":\"14770\",\"bantienmat\":\"\",\"banck\":\"15100\"},{\"type\":\"JPY\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/JPY.gif\",\"muatienmat\":\"190.7\",\"muack\":\"194.5\",\"bantienmat\":\"196.7\",\"banck\":\"197\"}]})";

        String url = "https://www.dongabank.com.vn/exchange/export";
        when(restTemplate.exchange(url,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        when(paymentGateWayDistributeService.handleGateWay(donateInformation.getMethodType(), donateInformation.getTransactionId()))
                .thenReturn(transactionResponse);



        paymentServiceTest.processPayment(donateInformation);

        assertThat(project.getCurrentMoney())
                .isGreaterThan(100000F)
                .isLessThan(800000F);
    }

    @Test
    public void testGetExchangeRate() throws DataProcessNotFoundException {
        String currency = "USD";
        String url = "https://www.dongabank.com.vn/exchange/export";

        String response = "({\"items\":[{\"type\":\"GBP\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/GBP.gif\",\"muatienmat\":\"29980\",\"muack\":\"30110\",\"bantienmat\":\"30500\",\"banck\":\"30490\"},{\"type\":\"AUD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/AUD.gif\",\"muatienmat\":\"15690\",\"muack\":\"15780\",\"bantienmat\":\"16000\",\"banck\":\"15990\"},{\"type\":\"CAD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/CAD.gif\",\"muatienmat\":\"17550\",\"muack\":\"17650\",\"bantienmat\":\"17880\",\"banck\":\"17870\"},{\"type\":\"CHF\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/CHF.gif\",\"muatienmat\":\"22610\",\"muack\":\"24120\",\"bantienmat\":\"23080\",\"banck\":\"24450\"},{\"type\":\"CNY\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/CNY.gif\",\"muatienmat\":\"3000\",\"muack\":\"3000\",\"bantienmat\":\"3500\",\"banck\":\"3500\"},{\"type\":\"EUR\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/EUR.gif\",\"muatienmat\":\"24940\",\"muack\":\"25050\",\"bantienmat\":\"25370\",\"banck\":\"25360\"},{\"type\":\"XAU\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/XAU.gif\",\"muatienmat\":\"6185000\",\"muack\":\"6220000\",\"bantienmat\":\"6255000\",\"banck\":\"6220000\"},{\"type\":\"HKD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/HKD.gif\",\"muatienmat\":\"2410\",\"muack\":\"2890\",\"bantienmat\":\"2920\",\"banck\":\"2930\"},{\"type\":\"USD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/USD.gif\",\"muatienmat\":\"22560\",\"muack\":\"22560\",\"bantienmat\":\"22740\",\"banck\":\"22740\"},{\"type\":\"THB\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/THB.gif\",\"muatienmat\":\"610\",\"muack\":\"670\",\"bantienmat\":\"680\",\"banck\":\"680\"},{\"type\":\"SGD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/SGD.gif\",\"muatienmat\":\"16440\",\"muack\":\"16590\",\"bantienmat\":\"16810\",\"banck\":\"16810\"},{\"type\":\"PNJ_DAB\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/PNJ_DAB.gif\",\"muatienmat\":\"5260000\",\"muack\":\"5260000\",\"bantienmat\":\"5340000\",\"banck\":\"5340000\"},{\"type\":\"NZD\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/NZD.gif\",\"muatienmat\":\"\",\"muack\":\"14770\",\"bantienmat\":\"\",\"banck\":\"15100\"},{\"type\":\"JPY\",\"imageurl\":\"https:\\/\\/www.dongabank.com.vn\\/images\\/flag\\/JPY.gif\",\"muatienmat\":\"190.7\",\"muack\":\"194.5\",\"bantienmat\":\"196.7\",\"banck\":\"197\"}]})";

        when(restTemplate.exchange(url,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // Call the function to test
        Float exchangeRate = paymentServiceTest.getExchangeRate(currency);

        assertThat(exchangeRate).isNotNull();
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
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(customUserDetail);

        when(userAccountDetailsService.findUserByIdentifier(userIdentifier))
                .thenReturn(userAccountDetail);

        return userAccountDetail;
    }
}
