package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.MailType;
import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.enums.TransactionStatus;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.core.exception.PayPalApiException;
import com.donation.common.management.entity.DonationTime;
import com.donation.common.management.entity.Project;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.security.CustomUserDetail;
import com.donation.common.security.SecurityUtils;
import com.donation.data.handler.service.*;
import com.donation.email.handler.enums.EmailTemplate;
import com.donation.email.handler.model.EmailBusinessDto;
import com.donation.email.handler.service.EmailDataHandlerService;
import com.donation.management.dto.ExchangeRateDto;
import com.donation.management.dto.ExchangeRateItemDto;
import com.donation.management.model.DonateInformation;
import com.donation.management.model.TransactionResponse;
import com.donation.payment.gateway.service.PaymentGateWayDistributeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.donation.common.core.constant.MessageConstants.*;

@Slf4j
@Service
public class PaymentServiceImpl extends BaseInitiatedServiceImpl
        implements PaymentService {
    private final String ANONYMOUS = "ANONYMOUS";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DonationTimeService donationTimeService;

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private GenerateIdService generateIdService;

    @Autowired
    private PaymentGateWayDistributeService paymentGateWayDistributeService;

    @Autowired
    private EmailDataHandlerService emailDataHandlerService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public DonateInformation checkProjectExist(DonateInformation donateInformation) throws InvalidMessageException {

        Project project = projectService.findProject(donateInformation.getProjectId());

        if (Objects.nonNull(project)) {
            donateInformation.setProjectName(project.getProjectName());
            return donateInformation;
        } else {
            throw new InvalidMessageException("Project invalid");
        }
    }

    @Override
    public void processPayment(DonateInformation donateInformation) throws PayPalApiException, InvalidMessageException, DataProcessNotFoundException {
        if (Objects.nonNull(donateInformation.getMethodType())
                && donateInformation.getMethodType().equals(PaymentMethodType.MANUAL)
                && Objects.isNull(donateInformation.getDonateAmount())) {
            throw new InvalidMessageException("Invalid amount");
        }

        String identifier = SecurityUtils.getUserIdentifier();

        UserAccountDetail userAccountDetail = userAccountDetailsService.findUserByIdentifier(identifier);

        if (Objects.nonNull(donateInformation.getProjectId())) {
            Project project = projectService.findProject(donateInformation.getProjectId());
            if (Objects.isNull(project)) {
                throw new InvalidMessageException("Cannot find project");
            }

            // Check project status
            ProjectStatusEnum projectStatus = project.getStatus();
            if (!checkValidProjectStatus(projectStatus)) {
                throw new InvalidMessageException(PROJECT_STATUS_EXCEPTION);
            }

            DonationTime donationTime = handlePayment(project, userAccountDetail, donateInformation);

            donationTimeService.updateTransfer(donationTime);

            // Update money for project
            updateTotalMoneyForProject(project, donationTime.getAmount());
        } else throw new InvalidMessageException("Invalid Project Code");
    }

    @Override
    public Float getExchangeRate(String currency) throws DataProcessNotFoundException {
        log.info("Getting exchange rate from bank...");
        String url = "https://www.dongabank.com.vn/exchange/export";

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, request, String.class);


        ExchangeRateDto exchangeRateDto;
        try {
            String jsonObject = Objects.requireNonNull(response.getBody())
                    .replace("(", StringUtils.EMPTY)
                    .replace(")", StringUtils.EMPTY);

            ObjectMapper objectMapper = new ObjectMapper();
            exchangeRateDto = objectMapper.readValue(jsonObject, ExchangeRateDto.class);

            ExchangeRateItemDto exchangeRateItemDto = exchangeRateDto.getItems().stream()
                    .filter(r -> r.getType().equalsIgnoreCase(currency))
                    .findFirst().orElse(null);

            return Float.parseFloat(exchangeRateItemDto.getBuyTransfer());
        } catch (Exception e) {
            throw new DataProcessNotFoundException("Cannot get exchange rate");
        }
    }

    private DonationTime handlePayment(Project project, UserAccountDetail userAccountDetail, DonateInformation donateInformation) throws PayPalApiException, InvalidMessageException, DataProcessNotFoundException {

        String transferId = generateIdService.generateTransferId(project.getProjectId());

        DonationTime donationTime;
        if (Objects.isNull(donateInformation.getMethodType())
                || donateInformation.getMethodType().equals(PaymentMethodType.MANUAL)) {
            log.info("Handling payment for manual method for {}", project.getProjectId());
            // Check email to remind
            if (Objects.isNull(donateInformation.getEmail())) {
                throw new InvalidMessageException(INVALID_EMAIL_EXCEPTION);
            }

            donationTime = DonationTime.builder()
                    .id(transferId)
                    .project(project)
                    .userAccountDetail(Objects.nonNull(userAccountDetail) ? userAccountDetail : null)
                    .supporterName(StringUtils.isNotEmpty(donateInformation.getSupporterName()) ? donateInformation.getSupporterName() : ANONYMOUS)
                    .supporterEmail(Objects.isNull(userAccountDetail) ? donateInformation.getEmail() : userAccountDetail.getEmail())
                    .isDisplayName((Objects.isNull(donateInformation.getIsDisplayName()) || donateInformation.getIsDisplayName()) ? Boolean.TRUE : Boolean.FALSE)
                    .amount(donateInformation.getDonateAmount())
                    .paymentMethod(PaymentMethodType.MANUAL)
                    .message(Objects.nonNull(donateInformation.getMessageDonate()) ? donateInformation.getMessageDonate() : null)
                    .status(TransactionStatus.PENDING)
                    .createdBy(Objects.nonNull(userAccountDetail) ? userAccountDetail.getUserAccount().getUsername() : "ADMIN")
                    .updatedBy(Objects.nonNull(userAccountDetail) ? userAccountDetail.getUserAccount().getUsername() : "ADMIN")
                    .build();

            // Send email to remind transfer money
            sendMailDonate(donationTime, EmailTemplate.EMAIL_REMIND_DONATE);
        } else {    // Gateway transaction
            log.info("Handling payment through gateway, all process will be handled by gateway for {}", project.getProjectId());
            // Check transaction was recognized
            DonationTime recognizedTransaction =
                    donationTimeService.findDonationTimeByTransactionCode(donateInformation.getTransactionId());

            if (Objects.nonNull(recognizedTransaction)) {
                throw new InvalidMessageException(TRANSACTION_DUPLICATE_EXCEPTION);
            }

            // Validate transaction from gateway
            TransactionResponse transactionResponse =
                    paymentGateWayDistributeService.handleGateWay(donateInformation.getMethodType(), donateInformation.getTransactionId());

            // Convert amount for total
            float amount;
            if (transactionResponse.getAmount().getCurrencyCode().equalsIgnoreCase("VND")) {
                amount = Float.parseFloat(transactionResponse.getAmount().getValue());
            } else {
                Float exchangeRate = getExchangeRate(transactionResponse.getAmount().getCurrencyCode());
                amount = Float.parseFloat(transactionResponse.getAmount().getValue()) * exchangeRate;
            }

            donationTime = DonationTime.builder()
                    .id(transferId)
                    .project(project)
                    .userAccountDetail(Objects.nonNull(userAccountDetail) ? userAccountDetail : null)
                    .transactionCode(transactionResponse.getId())
                    .supporterName(StringUtils.isNotEmpty(donateInformation.getSupporterName()) ? donateInformation.getSupporterName() : ANONYMOUS)
                    .supporterEmail(Objects.isNull(userAccountDetail) ? donateInformation.getEmail() : userAccountDetail.getEmail())
                    .isDisplayName((Objects.isNull(donateInformation.getIsDisplayName()) || donateInformation.getIsDisplayName()) ? Boolean.TRUE : Boolean.FALSE)
                    .amount(amount)
                    .paymentMethod(donateInformation.getMethodType())
                    .message(Objects.nonNull(donateInformation.getMessageDonate()) ? donateInformation.getMessageDonate() : null)
                    .status(transactionResponse.getStatus().equals("COMPLETED") ? TransactionStatus.COMPLETE : TransactionStatus.PENDING)
                    .createdBy(Objects.nonNull(userAccountDetail) ? userAccountDetail.getUserAccount().getUsername() : "ADMIN")
                    .updatedBy(Objects.nonNull(userAccountDetail) ? userAccountDetail.getUserAccount().getUsername() : "ADMIN")
                    .build();

            // Send email to confirm completion donate
            sendMailDonate(donationTime, EmailTemplate.EMAIL_CONFIRM_DONATE);
        }
        return donationTime;
    }

    private void sendMailDonate(DonationTime donationTime, EmailTemplate emailTemplate) {
        log.info("Email for {} is preparing with template {}", donationTime.getProject().getProjectId(), emailTemplate.templateFileName);
        EmailBusinessDto emailBusinessDto = EmailBusinessDto.builder()
                .mailType(MailType.DONATE)
                .userName(donationTime.getSupporterName())
                .projectId(donationTime.getProject().getProjectId())
                .projectName(donationTime.getProject().getProjectName())
                .amount(donationTime.getAmount())
                .currency("VND")
                .transferCode(donationTime.getId())
                .build();

        List<String> toUser = Collections.singletonList(donationTime.getSupporterEmail());

        emailDataHandlerService.handleDataAndSendMail(emailBusinessDto, emailTemplate,
                toUser, new ArrayList<>(), emailTemplate.baseSubject);

    }
}
