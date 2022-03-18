package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.management.entity.*;
import com.donation.common.management.mapper.BeneficiaryMapper;
import com.donation.common.management.repository.BeneficiaryRepository;
import com.donation.common.security.CustomUserDetail;
import com.donation.data.handler.service.BeneficiaryService;
import com.donation.data.handler.service.GenerateIdService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.impl.BeneficiaryServiceImpl;
import com.donation.data.handler.service.impl.GenerateIdServiceImpl;
import com.donation.data.handler.service.impl.UserAccountDetailsServiceImpl;
import com.donation.management.dto.BeneficiaryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BeneficiaryServiceImplTest {
    @InjectMocks
    private BeneficiaryService beneficiaryServiceTest = new BeneficiaryServiceImpl();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Mock
    private GenerateIdService generateIdService = new GenerateIdServiceImpl();

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Spy
    private BeneficiaryMapper beneficiaryMapper = Mappers.getMapper(BeneficiaryMapper.class);

    @Test
    public void testGetBeneficiaries() {

        when(beneficiaryRepository.findAll())
                .thenReturn(List.of(Beneficiary.builder()
                        .build()));

        // Call the function to test
        List<BeneficiaryDto> beneficiaries = beneficiaryServiceTest.getBeneficiaries();

        assertThat(beneficiaries).hasSize(1);
    }

    @Test
    public void testGetBeneficiary() {

        String beneficiaryId = "AB-2100001";
        when(beneficiaryRepository.findById(beneficiaryId))
                .thenReturn(Optional.ofNullable(Beneficiary.builder()
                        .beneficiaryId(beneficiaryId)
                        .build()));

        // Call the function to test
        Beneficiary beneficiary = beneficiaryServiceTest.getBeneficiary(beneficiaryId);

        assertThat(beneficiary).isNotNull();
    }

    @Test
    public void testCreateBeneficiary() throws Exception {

        buildForAdmin();

        BeneficiaryDto beneficiaryDto = BeneficiaryDto.builder()
                .beneficiaryId("AB-2100001")
                .beneficiaryName("Athony Blame")
                .beneficiaryPhoneNumber("0909090909")
                .build();

        // Call the function to test
        beneficiaryServiceTest.createBeneficiary(beneficiaryDto);

        ArgumentCaptor<Beneficiary> beneficiaryArgumentCaptor =
                ArgumentCaptor.forClass(Beneficiary.class);

        verify(beneficiaryRepository).save(beneficiaryArgumentCaptor.capture());

        Beneficiary beneficiary = beneficiaryArgumentCaptor.getValue();

        assertThat(beneficiary).isNotNull();
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
