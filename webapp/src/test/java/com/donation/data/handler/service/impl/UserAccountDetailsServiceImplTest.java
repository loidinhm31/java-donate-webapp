package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.AuthenticationType;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.UserAccount;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.mapper.SearchUserMapper;
import com.donation.common.management.mapper.UserAccountDetailMapper;
import com.donation.common.management.repository.UserAccountDetailRepository;
import com.donation.common.management.repository.UserAccountRepository;
import com.donation.common.management.repository.projection.UserAccountProjection;
import com.donation.common.security.CustomUserDetail;
import com.donation.common.security.service.RoleService;
import com.donation.common.security.service.impl.RoleServiceImpl;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.impl.UserAccountDetailsServiceImpl;
import com.donation.email.handler.service.EmailDataHandlerService;
import com.donation.email.handler.service.impl.EmailDataHandlerServiceImpl;
import com.donation.management.dto.UserAccountDetailDto;
import com.donation.management.dto.UserAccountDto;
import com.donation.management.dto.UserRequestDto;
import com.donation.management.model.UserFilter;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAccountDetailsServiceImplTest {

    @InjectMocks
    private UserAccountDetailsService userAccountDetailsServiceTest = new UserAccountDetailsServiceImpl();

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAccountDetailRepository userAccountDetailRepository;

    @Mock
    private EmailDataHandlerService emailDataHandlerService = new EmailDataHandlerServiceImpl();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Mock
    private RoleService roleService = new RoleServiceImpl();

    @Spy
    private UserAccountDetailMapper userAccountDetailMapper = Mappers.getMapper(UserAccountDetailMapper.class);

    @Spy
    private SearchUserMapper searchUserMapper = Mappers.getMapper(SearchUserMapper.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateAuthenticationType() {
        // Build input
        AuthenticationType authenticationType = AuthenticationType.BASIC;
        UserAccountDetail userDetails = UserAccountDetail.builder()
                .userId(UUID.randomUUID())
                .build();

        // Call the function to test
        userAccountDetailsServiceTest.updateAuthenticationType(userDetails, authenticationType);

        ArgumentCaptor<UserAccountDetail> userAccountDetailArgumentCaptor =
                ArgumentCaptor.forClass(UserAccountDetail.class);
        verify(userAccountDetailRepository)
                .save(userAccountDetailArgumentCaptor.capture());

        UserAccountDetail capturedUserAccountDetail = userAccountDetailArgumentCaptor.getValue();

        assertThat(capturedUserAccountDetail.getAuthenticationType())
                .isEqualTo(AuthenticationType.BASIC);

        Calendar calendar = Calendar.getInstance();
        assertThat(capturedUserAccountDetail.getLastedLoginTime())
                .isNotNull()
                .hasYear(calendar.get(Calendar.YEAR))
                .hasMonth(calendar.get(Calendar.MONTH) + 1)
                .hasDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testIsUsernameUnique() {
        String username = "root";
        UserAccount userAccount = UserAccount.builder()
                .userId(UUID.randomUUID())
                .username(username)
                .build();

        when(userAccountRepository.findByUsername(username)).thenReturn(userAccount);

        // Call the function to test
        Boolean isUsernameUnique = userAccountDetailsServiceTest.isUsernameUnique(username);

        assertThat(isUsernameUnique).isFalse();

    }

    @Test
    public void testIsEmailUnique() {
        String email = "root@gmail.com";
        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(UUID.randomUUID())
                .email(email)
                .build();

        when(userAccountDetailRepository.findByEmail(email)).thenReturn(userAccountDetail);

        // Call the function to test
        Boolean isEmailUnique = userAccountDetailsServiceTest.isEmailUnique(email);

        assertThat(isEmailUnique).isFalse();
    }

    @Test
    public void testCreateUser() throws ParseException, InvalidMessageException {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRequestURI("localhost:7070/");

        String username = "root";
        String password = "root";
        String email = "root@gmail.com";
        Date birthdate = new SimpleDateFormat("yyyy-MM-dd").parse("1999-06-27");

        UserRequestDto userRequestDto = UserRequestDto.builder()
                .username(username)
                .email(email)
                .birthdate(birthdate)
                .password(password)
                .build();

        // Call the function to test
        userAccountDetailsServiceTest.createUser(mockHttpServletRequest, userRequestDto);

        ArgumentCaptor<UserAccountDetail> userAccountDetailArgumentCaptor =
                ArgumentCaptor.forClass(UserAccountDetail.class);

        verify(userAccountDetailRepository)
                .save(userAccountDetailArgumentCaptor.capture());

        UserAccountDetail capturedUserAccountDetail = userAccountDetailArgumentCaptor.getValue();

        assertThat(capturedUserAccountDetail.getUserAccount()).isNotNull();
        assertThat(capturedUserAccountDetail.getVerificationCode()).isNotEmpty();
        assertThat(capturedUserAccountDetail.getUserAccount().getIsVerify()).isFalse();
    }

    @Test
    public void testVerify() {
        String verifyToken = RandomString.make(64);

        UUID userId = UUID.randomUUID();
        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .isVerify(Boolean.FALSE)
                .build();
        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(userId)
                .userAccount(userAccount)
                .verificationCode(verifyToken)
                .build();
        userAccount.setUserAccountDetail(userAccountDetail);

        when(userAccountDetailRepository.findByVerificationCode(verifyToken))
                .thenReturn(userAccountDetail);

        // Call the function to test
        Boolean isVerified = userAccountDetailsServiceTest.verify(verifyToken);
        assertThat(isVerified).isTrue();
    }

    @Test
    public void testGetUser() throws InvalidMessageException {
        UUID userId = UUID.randomUUID();
        Optional<UserAccountDetail> optionalUserAccountDetail = Optional.ofNullable(UserAccountDetail.builder()
                .userId(userId)
                .build());

        when(userAccountDetailRepository.findById(userId))
                .thenReturn(optionalUserAccountDetail);

        // Call the function to test
        UserAccountDetailDto userAccountDetailDto = userAccountDetailsServiceTest.getUser(userId);

        assertThat(userAccountDetailDto).isNotNull();
    }

    @Test
    public void testGetUsers() {
        UserFilter userFilter = UserFilter.builder()
                .startPage(0)
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();

        Page<UserAccountProjection> users = mock(Page.class);
        when(userAccountDetailRepository.searchUsers(userFilter)).thenReturn(users);
        // Call the function to test
        Page<UserAccountDetailDto> userAccountDetailDtos = userAccountDetailsServiceTest.getUsers(userFilter);
    }

    @Test
    public void testUpdateResetPasswordToken() throws InvalidMessageException {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRequestURI("localhost:7070/");

        String email = "root@gmail.com";

        UUID userId = UUID.randomUUID();
        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .username("root")
                .build();
        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(userId)
                .userAccount(userAccount)
                .firstName("TEST")
                .lastName("TEST")
                .email(email)
                .build();
        userAccount.setUserAccountDetail(userAccountDetail);

        when(userAccountDetailRepository.findByEmail(email))
                .thenReturn(userAccountDetail);

        // Call the function to test
        userAccountDetailsServiceTest.updateResetPasswordToken(mockHttpServletRequest, email);

        ArgumentCaptor<UserAccountDetail> userAccountDetailArgumentCaptor =
                ArgumentCaptor.forClass(UserAccountDetail.class);

        verify(userAccountDetailRepository).save(userAccountDetailArgumentCaptor.capture());

        UserAccountDetail capturedUserAccountDetail1 = userAccountDetailArgumentCaptor.getValue();

        assertThat(capturedUserAccountDetail1.getResetToken()).isNotEmpty();
    }

    @Test
    public void testGetByResetPasswordToken() {
        String token = RandomString.make(30);
        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(UUID.randomUUID())
                .resetToken(token)
                .build();

        when(userAccountDetailRepository.findByResetToken(token)).thenReturn(userAccountDetail);

        // Call the function to test
        UserAccountDetailDto userAccountDetailDto = userAccountDetailsServiceTest.getByResetPasswordToken(token);

        assertThat(userAccountDetailDto).isNotNull();
    }

    @Test
    public void testUpdatePassword() throws InvalidMessageException {
        String token = RandomString.make(30);
        String password = "root123456";

        UUID userId = UUID.randomUUID();
        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .username("root")
                .build();
        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(userId)
                .userAccount(userAccount)
                .firstName("TEST")
                .lastName("TEST")
                .resetToken(token)
                .build();
        userAccount.setUserAccountDetail(userAccountDetail);

        when(userAccountDetailRepository.findByResetToken(token)).thenReturn(userAccountDetail);

        // Call the function to test
        userAccountDetailsServiceTest.updatePassword(token, password);

        ArgumentCaptor<UserAccountDetail> userAccountDetailArgumentCaptor =
                ArgumentCaptor.forClass(UserAccountDetail.class);

        verify(userAccountDetailRepository).save(userAccountDetailArgumentCaptor.capture());

        UserAccountDetail capturedUserAccountDetail1 = userAccountDetailArgumentCaptor.getValue();

        assertThat(capturedUserAccountDetail1.getResetToken()).isNull();
        assertThat(capturedUserAccountDetail1.getUserAccount().getUserPassword()).isNotEmpty();
    }

    @Test
    public void testUpdateUserAccount() throws InvalidMessageException {

        String userIdentifier = "root";

        UserAccount userAccount = UserAccount.builder()
                .username(userIdentifier)
                .userPassword("root")
                .build();

        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userAccount(userAccount)
                .build();

        // Custom authentication
        CustomUserDetail customUserDetail =
                new CustomUserDetail(userAccount);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(customUserDetail);


        UserAccountDto userAccountDto = UserAccountDto.builder()
                .username(userIdentifier)
                .build();
        UserAccountDetailDto userAccountDetailDto = UserAccountDetailDto.builder()
                .userAccount(userAccountDto)
                .firstName("NEW TEST")
                .lastName("NEW TEST")
                .password("NEWPASSWORD")
                .phoneNumber("0909090909")
                .build();

        when(userAccountDetailRepository.findByEmailOrUsername(userIdentifier)).thenReturn(userAccountDetail);

        // Call the function to test
        userAccountDetailsServiceTest.updateUserAccount(userAccountDetailDto);

        ArgumentCaptor<UserAccountDetail> userAccountDetailArgumentCaptor =
                ArgumentCaptor.forClass(UserAccountDetail.class);

        verify(userAccountDetailRepository)
                .save(userAccountDetailArgumentCaptor.capture());

        UserAccountDetail capturedUserAccountDetail = userAccountDetailArgumentCaptor.getValue();

        assertThat(userAccountDetailMapper.toDto(capturedUserAccountDetail).getFirstName())
                .isEqualTo("NEW TEST");

        assertThat(userAccountDetailMapper.toDto(capturedUserAccountDetail).getLastName())
                .isEqualTo("NEW TEST");

        assertThat(userAccountDetailMapper.toDto(capturedUserAccountDetail).getPhoneNumber())
                .isEqualTo("0909090909");

        assertThat(capturedUserAccountDetail.getUserAccount().getUserPassword()).isNotEmpty();
    }

    @Test
    public void testFindUserAccountDetailByEmail() {
        String email = "root@gmail.com";

        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(UUID.randomUUID())
                .email(email)
                .build();

        when(userAccountDetailRepository.findByEmail(email))
                .thenReturn(userAccountDetail);

        // Call the function to test
        UserAccountDetail savedAccountDetail =
                userAccountDetailsServiceTest.findUserAccountDetailByEmail(email);

        assertThat(savedAccountDetail).isNotNull();
    }

    @Test
    public void testCreateUserByOAuthLogin() {
        String name = "TEST APP";
        String email = "root123@gmail.com";
        AuthenticationType authenticationType = AuthenticationType.GOOGLE;

        when(userAccountRepository.findByMaxIdUsernameByUsername("root"))
                .thenReturn(List.of("root1"));

        // Call the function to test
        userAccountDetailsServiceTest.createUserByOAuthLogin(name, email, authenticationType);

        ArgumentCaptor<UserAccountDetail> userAccountDetailArgumentCaptor =
                ArgumentCaptor.forClass(UserAccountDetail.class);

        verify(userAccountDetailRepository).save(userAccountDetailArgumentCaptor.capture());

        UserAccountDetail capturedUserAccountDetail = userAccountDetailArgumentCaptor.getValue();

        assertThat(capturedUserAccountDetail.getUserAccount()).isNotNull();
        assertThat(capturedUserAccountDetail.getUserAccount().getUsername()).isEqualTo("root2");
        assertThat(capturedUserAccountDetail.getAuthenticationType()).isEqualTo(authenticationType);
        assertThat(capturedUserAccountDetail.getEmail()).isEqualTo(email);
        assertThat(capturedUserAccountDetail.getFirstName()).isEqualTo("TEST");
        assertThat(capturedUserAccountDetail.getLastName()).isEqualTo("APP");
    }

    @Test
    public void testGetUserByIdentifier() throws InvalidMessageException {
        String userIdentifier = "root";

        UserAccount userAccount = UserAccount.builder()
                .username(userIdentifier)
                .userPassword("root")
                .build();

        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userAccount(userAccount)
                .build();

        when(userAccountDetailRepository.findByEmailOrUsername(userIdentifier))
                .thenReturn(userAccountDetail);

        // Call the function to test
        UserAccountDetailDto userAccountDetailDto =
                userAccountDetailsServiceTest.getUserByIdentifier(userIdentifier);

        assertThat(userAccountDetailDto.getUserAccount().getUsername()).isEqualTo(userIdentifier);
    }

    @Test
    public void testFindUserByIdentifier() {
        String userIdentifier = "root";

        UserAccount userAccount = UserAccount.builder()
                .username(userIdentifier)
                .userPassword("root")
                .build();

        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userAccount(userAccount)
                .build();

        when(userAccountDetailRepository.findByEmailOrUsername(userIdentifier))
                .thenReturn(userAccountDetail);

        // Call the function to test
        UserAccountDetail savedUserAccountDetail =
                userAccountDetailsServiceTest.findUserByIdentifier(userIdentifier);

        assertThat(savedUserAccountDetail).isNotNull();
        assertThat(savedUserAccountDetail.getUserAccount().getUsername())
                .isEqualTo(userIdentifier);
    }
 }
