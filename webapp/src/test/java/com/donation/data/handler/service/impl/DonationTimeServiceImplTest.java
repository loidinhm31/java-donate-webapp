package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.enums.TransactionStatus;
import com.donation.common.management.entity.*;
import com.donation.common.management.mapper.DonationTimeMapper;
import com.donation.common.management.repository.DonationTimeRepository;
import com.donation.common.security.CustomUserDetail;
import com.donation.data.handler.service.DonationTimeService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.impl.DonationTimeServiceImpl;
import com.donation.data.handler.service.impl.UserAccountDetailsServiceImpl;
import com.donation.management.dto.DonationTimeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DonationTimeServiceImplTest {
    @InjectMocks
    private DonationTimeService donationTimeServiceTest = new DonationTimeServiceImpl();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Mock
    private DonationTimeRepository donationTimeRepository;

    @Spy
    private DonationTimeMapper donationTimeMapper = Mappers.getMapper(DonationTimeMapper.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetDonators() {
        String projectId = "NC-2100001";

        Project project = Project.builder()
                .projectId(projectId)
                .build();

        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

        Page<DonationTime> donationTimeList = new PageImpl<>(
                List.of(DonationTime.builder()
                        .project(project)
                        .isDisplayName(Boolean.FALSE)
                        .build())
        );

        when(donationTimeRepository.findDonationTimeByProjectAndStatus(projectId,
                TransactionStatus.COMPLETE.name(), pageable))
                .thenReturn(donationTimeList);

        // Call the function to test
        Page<DonationTimeDto> donationTimeDtos =
                donationTimeServiceTest.getDonators(projectId, pageable);

        assertThat(donationTimeDtos.getTotalElements())
                .isEqualTo(donationTimeList.getTotalElements());
    }

    @Test
    public void testFindDonationTime() {
        String transferCode = "NC22011819101011";
        String projectId = "NC-2100001";

        when(donationTimeRepository.findByIdAndProjectId(transferCode, projectId))
                .thenReturn(DonationTime.builder()
                        .id(transferCode)
                        .project(Project.builder()
                                .projectId(projectId)
                                .build())
                        .build());

        // Call the function to test
        DonationTime savedDonationTime =
                donationTimeServiceTest.findDonationTime(transferCode, projectId);

        assertThat(savedDonationTime).isNotNull();
    }

    @Test
    public void testUpdateTransfer() {
        DonationTime donationTime = DonationTime.builder()
                .status(TransactionStatus.COMPLETE)
                .build();

        // Call the function to test
        donationTimeServiceTest.updateTransfer(donationTime);

        ArgumentCaptor<DonationTime> donationTimeArgumentCaptor =
                ArgumentCaptor.forClass(DonationTime.class);

        verify(donationTimeRepository)
                .save(donationTimeArgumentCaptor.capture());

        DonationTime savedDonationTime = donationTimeArgumentCaptor.getValue();

        assertThat(savedDonationTime.getStatus())
                .isEqualTo(TransactionStatus.COMPLETE);
    }

    @Test
    public void testGetDonationTimeByUser() {
        UserAccountDetail userAccountDetail = buildForUser();

        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.Direction.DESC, "created_at");

        Page<DonationTime> donationTimes = new PageImpl<>(
                List.of(DonationTime.builder()
                        .build())
        );

        when(donationTimeRepository.findDonationTimeByUser(userAccountDetail.getUserId(),
                pageable))
                .thenReturn(donationTimes);

        // Call the function to test
        Page<DonationTimeDto> donationTimeDtos =
                donationTimeServiceTest.getDonationTimeByUser(0, DEFAULT_PAGE_SIZE);

        assertThat(donationTimeDtos.getTotalElements())
                .isEqualTo(donationTimes.getTotalElements());
    }

    @Test
    public void testFindDonationTimeByTransactionCode() {
        String transactionId = "ABCXYZ101010";

        when(donationTimeRepository.findByTransactionCode(transactionId))
                .thenReturn(DonationTime.builder()
                        .transactionCode(transactionId)
                        .build());

        // Call the function to test
        DonationTime donationTime =
                donationTimeServiceTest.findDonationTimeByTransactionCode(transactionId);

        assertThat(donationTime).isNotNull();
    }

    @Test
    public void testUpdateAll() {

        List<DonationTime> donationTimeList = List.of(
                DonationTime.builder()
                        .build()
        );

        // Call the function to test
        donationTimeServiceTest.updateAll(donationTimeList);

        ArgumentCaptor<List<DonationTime>> listArgumentCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(donationTimeRepository)
                .saveAll(listArgumentCaptor.capture());

        List<DonationTime> listArgumentCaptorValue = listArgumentCaptor.getValue();

        assertThat(listArgumentCaptorValue).isNotNull();
    }

    private UserAccountDetail buildForUser() {
        // Custom authentication
        String userIdentifier = "user";
        UUID userId = UUID.randomUUID();

        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .username(userIdentifier)
                .userPassword("user")
                .userRoles(List.of(UserRole.builder()
                        .role(Role.builder()
                                .roleId(UUID.randomUUID())
                                .roleCode(RoleEnum.CLIENT)
                                .roleName(RoleEnum.CLIENT.getRoleName())
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
