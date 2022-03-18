package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.AuthenticationType;
import com.donation.common.core.enums.MailType;
import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.core.utils.ServerUtility;
import com.donation.common.management.entity.Role;
import com.donation.common.management.entity.UserAccount;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.entity.UserRole;
import com.donation.common.management.mapper.SearchUserMapper;
import com.donation.common.management.mapper.UserAccountDetailMapper;
import com.donation.common.management.repository.UserAccountDetailRepository;
import com.donation.common.management.repository.UserAccountRepository;
import com.donation.common.management.repository.projection.UserAccountProjection;
import com.donation.common.security.SecurityUtils;
import com.donation.common.security.service.RoleService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.email.handler.enums.EmailTemplate;
import com.donation.email.handler.model.EmailBusinessDto;
import com.donation.email.handler.service.EmailDataHandlerService;
import com.donation.management.dto.UserAccountDetailDto;
import com.donation.management.dto.UserRequestDto;
import com.donation.management.model.UserFilter;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

import static com.donation.common.core.constant.MessageConstants.AUTHORIZE_EXCEPTION;
import static com.donation.common.core.constant.UrlMapping.VERIFY_PATH;

@Slf4j
@Transactional(rollbackOn = Exception.class)
@Service
public class UserAccountDetailsServiceImpl implements UserAccountDetailsService {

    @Autowired
    private UserAccountDetailRepository userAccountDetailRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EmailDataHandlerService emailDataHandlerService;

    @Autowired
    private SearchUserMapper searchUserMapper;

    @Autowired
    private UserAccountDetailMapper userAccountDetailMapper;

    @Override
    public void updateAuthenticationType(UserAccountDetail userDetails, AuthenticationType authenticationType) {
        userDetails.setAuthenticationType(authenticationType);
        userDetails.setLastedLoginTime(new Date());
        userAccountDetailRepository.save(userDetails);
    }

    @Override
    public boolean isUsernameUnique(String username) {
        UserAccount userAccountDetails = userAccountRepository.findByUsername(username);
        return Objects.isNull(userAccountDetails);
    }

    @Override
    public boolean isEmailUnique(String email) {
        UserAccountDetail userAccountDetail = userAccountDetailRepository.findByEmail(email);
        return Objects.isNull(userAccountDetail);
    }

    @Override
    public void createUser(HttpServletRequest servletRequest, UserRequestDto userRequestDto) throws InvalidMessageException {
        if (Objects.nonNull(userRequestDto)
                && Objects.nonNull(userRequestDto.getUsername())
                && Objects.nonNull(userRequestDto.getEmail())
                && Objects.nonNull(userRequestDto.getBirthdate())) {

            checkDateOfBirth(userRequestDto.getBirthdate());

            checkUsername(userRequestDto.getUsername());

            checkUserEmail(userRequestDto.getEmail());

            UserAccount userAccount = new UserAccount();
            userAccount.setUsername(userRequestDto.getUsername());

            String encodedPassword = userAccount.passwordEncoder().encode(userRequestDto.getPassword());
            userAccount.setUserPassword(encodedPassword);
            userAccount.setIsVerify(Boolean.FALSE);
            userAccount.setIsActive(Boolean.FALSE);

            String randomCode = RandomString.make(64);
            UserAccountDetail userAccountDetail = new UserAccountDetail();
            userAccountDetail.setEmail(userRequestDto.getEmail());
            userAccountDetail.setFirstName(userRequestDto.getFirstName());
            userAccountDetail.setLastName(userRequestDto.getLastName());
            userAccountDetail.setBirthdate(userRequestDto.getBirthdate());
            userAccountDetail.setPhoneNumber(Objects.nonNull(userRequestDto.getPhoneNumber()) ? userRequestDto.getPhoneNumber() : null);
            userAccountDetail.setVerificationCode(randomCode);
            userAccountDetail.setCreatedBy(userRequestDto.getUsername());
            userAccountDetail.setUpdatedBy(userRequestDto.getUsername());

            userAccountDetail.setUserAccount(userAccount);
            userAccountDetailRepository.save(userAccountDetail);

            handleVerifyMailForUser(servletRequest, userAccountDetail);
        }
    }

    @Override
    public boolean verify(String code) {
        UserAccountDetail userAccountDetail = userAccountDetailRepository.findByVerificationCode(code);

        if (Objects.nonNull(userAccountDetail)) {
            UserAccount userAccount = userAccountDetail.getUserAccount();
            if (!userAccount.getIsVerify()) {
                // Clear token or make token expire
                userAccount.getUserAccountDetail().setVerificationCode(null);
                userAccount.setIsVerify(Boolean.TRUE);
                userAccount.setIsActive(Boolean.TRUE);

                // Set default role
                Role defaultRole = roleService.findRole(RoleEnum.CLIENT);
                UserRole userRole = new UserRole();
                userRole.setUserAccount(userAccount);
                userRole.setRole(defaultRole);

                List<UserRole> userRoles = new ArrayList<>();
                userRoles.add(userRole);
                userAccount.setUserRoles(userRoles);

                userAccountRepository.save(userAccount);
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public UserAccountDetailDto getUser(UUID userId) throws InvalidMessageException {
        Optional<UserAccountDetail> userAccountDetail = userAccountDetailRepository.findById(userId);

        if (userAccountDetail.isEmpty()) {
            throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
        }
        return userAccountDetailMapper.toDto(userAccountDetail.get());
    }

    @Override
    public Page<UserAccountDetailDto> getUsers(UserFilter userFilter) {
        Page<UserAccountProjection> users =
                userAccountDetailRepository.searchUsers(userFilter);

        Page<UserAccountDetailDto> userAccountDetailDtos =
                users.map(searchUserMapper::toDto);

        return userAccountDetailDtos;
    }

    @Override
    public void updateResetPasswordToken(HttpServletRequest servletRequest, String email) throws InvalidMessageException {

        UserAccountDetail userAccountDetail = userAccountDetailRepository.findByEmail(email);
        if (Objects.nonNull(userAccountDetail)) {
            String token = RandomString.make(30);
            userAccountDetail.setResetToken(token);
            userAccountDetailRepository.save(userAccountDetail);

            // Send reset password email
            handleResetPasswordMailForUser(servletRequest, userAccountDetail);

        } else {
            throw new InvalidMessageException("Could not find any user with this email " + email);
        }
    }

    @Override
    public UserAccountDetailDto getByResetPasswordToken(String token) {
        UserAccountDetail userAccountDetail = userAccountDetailRepository.findByResetToken(token);

        return userAccountDetailMapper.toDto(userAccountDetail);
    }

    @Override
    public void updatePassword(String token, String password) throws InvalidMessageException {
        UserAccountDetail userAccountDetail = userAccountDetailRepository.findByResetToken(token);
        if (Objects.isNull(userAccountDetail)) {
            throw new InvalidMessageException("No user found: invalid token");
        }

        String encodedPassword = userAccountDetail.getUserAccount().passwordEncoder().encode(password);
        userAccountDetail.getUserAccount().setUserPassword(encodedPassword);
        userAccountDetail.setResetToken(null);

        userAccountDetailRepository.save(userAccountDetail);
    }

    @Override
    public UserAccountDetailDto updateUserAccount(UserAccountDetailDto userAccountDetailDto) throws InvalidMessageException {
        String userIdentifier = SecurityUtils.getUserIdentifier();
        UserAccountDetail userAccountDetail =
                userAccountDetailRepository.findByEmailOrUsername(userIdentifier);

        if (Objects.nonNull(userAccountDetail)) {
            if (StringUtils.isEmpty(userAccountDetailDto.getFirstName())) {
                throw new InvalidMessageException("Invalid First Name");
            } else {
                userAccountDetail.setFirstName(userAccountDetailDto.getFirstName());
            }

            if (StringUtils.isEmpty(userAccountDetailDto.getLastName())) {
                throw new InvalidMessageException("Invalid Last Name");
            } else {
                userAccountDetail.setLastName(userAccountDetailDto.getLastName());
            }

            // Set new password
            if (StringUtils.isNotEmpty(userAccountDetailDto.getPassword())) {
                String encodedPassword = userAccountDetail.getUserAccount().passwordEncoder().encode(userAccountDetailDto.getPassword());
                userAccountDetail.getUserAccount().setUserPassword(encodedPassword);
            }

            if (StringUtils.isNotEmpty(userAccountDetailDto.getPhoneNumber())) {
                userAccountDetail.setPhoneNumber(userAccountDetailDto.getPhoneNumber());
            }

            UserAccountDetail savedUserAccountDetail =
                    userAccountDetailRepository.save(userAccountDetail);
            return userAccountDetailMapper.toDto(savedUserAccountDetail);
        } else throw new InvalidMessageException("User is not exist");

    }

    @Override
    public UserAccountDetail findUserAccountDetailByEmail(String email) {
        return userAccountDetailRepository.findByEmail(email);
    }

    @Override
    public void createUserByOAuthLogin(String name, String email,
                                       AuthenticationType authenticationType) {

        String freshUsername = createIncreasedUsername(email);

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(freshUsername);
        userAccount.setUserPassword(StringUtils.EMPTY);
        userAccount.setIsVerify(Boolean.TRUE);
        userAccount.setIsActive(Boolean.TRUE);

        // Set default role
        Role defaultRole = roleService.findRole(RoleEnum.CLIENT);
        UserRole userRole = new UserRole();
        userRole.setUserAccount(userAccount);
        userRole.setRole(defaultRole);
        List<UserRole> userRoles = List.of(userRole);
        userAccount.setUserRoles(userRoles);

        UserAccountDetail userAccountDetail = new UserAccountDetail();
        setName(name, userAccountDetail);
        userAccountDetail.setEmail(email);
        userAccountDetail.setAuthenticationType(authenticationType);
        userAccountDetail.setBirthdate(new Date());
        userAccountDetail.setCreatedBy(freshUsername);
        userAccountDetail.setUpdatedBy(freshUsername);

        userAccountDetail.setUserAccount(userAccount);
        userAccountDetailRepository.save(userAccountDetail);
    }

    @Override
    public UserAccountDetailDto getUserByIdentifier(String identifier) throws InvalidMessageException {
        UserAccountDetail userAccountDetail = userAccountDetailRepository.findByEmailOrUsername(identifier);

        if (Objects.isNull(userAccountDetail)) {
            throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
        }

        return userAccountDetailMapper.toDto(userAccountDetail);
    }

    @Override
    public UserAccountDetail findUserByIdentifier(String identifier) {
        UserAccountDetail userAccountDetail = userAccountDetailRepository.findByEmailOrUsername(identifier);

        return userAccountDetail;
    }

    private void setName(String name, UserAccountDetail customer) {
        String[] nameArray = name.split(StringUtils.SPACE);
        if (nameArray.length < 2) {
            customer.setFirstName(name);
            customer.setLastName(StringUtils.EMPTY);
        } else {
            String firstName = nameArray[0];
            customer.setFirstName(firstName);

            String lastName = name.replaceFirst(firstName + StringUtils.SPACE, StringUtils.EMPTY);
            customer.setLastName(lastName);
        }
    }

    private String createIncreasedUsername(String email) {
        String[] emailComponents = email.split("@");
        String username = emailComponents[0];

        String number = username.replaceAll("[^0-9]", ";");
        String[] numberArrays = number.split(";");

        String onlyUsername = username;
        if (numberArrays.length > 0) {
            String lastArr = numberArrays[numberArrays.length - 1];

            int mainLength = username.length();
            int subIndex = username.length();
            for (int i = lastArr.length(); i > 0; i--) {
                if (lastArr.charAt(i - 1) == username.charAt(mainLength - 1)) {
                    subIndex = mainLength - 1;
                }
                mainLength--;
            }
            onlyUsername = username.substring(0, subIndex);
        }

        List<String> maxIdUsername =
                userAccountRepository.findByMaxIdUsernameByUsername(onlyUsername);

        String finalOnlyUsername = onlyUsername;
        long maxId = maxIdUsername.stream()
                .filter(Objects::nonNull)
                .filter(o -> o.startsWith(finalOnlyUsername))
                .map(o -> o.replaceFirst(finalOnlyUsername, StringUtils.EMPTY))
                .mapToLong(o -> {
                    try {
                        return Long.parseLong(o);
                    } catch (NumberFormatException e) {
                        return 0L;
                    }
                })
                .max()
                .orElse(0L);

        return onlyUsername + (maxId + 1);
    }

    private void checkUsername(String username) throws InvalidMessageException {
        UserAccount createdUserAccount = userAccountRepository.findByUsername(username);
        if (Objects.nonNull(createdUserAccount)) {
            throw new InvalidMessageException("Username has been used");
        }
    }

    private void checkUserEmail(String email) throws InvalidMessageException {
        UserAccountDetail createdUserAccount = userAccountDetailRepository.findByEmail(email);
        if (Objects.nonNull(createdUserAccount)) {
            throw new InvalidMessageException("Email has been used");
        }
    }

    private void checkDateOfBirth(Date birthdate) throws InvalidMessageException {
        if (Objects.nonNull(birthdate)
                && birthdate.after(new Date())) {
            throw new InvalidMessageException("Date of birth cannot greater than today");
        }
    }

    private void handleVerifyMailForUser(HttpServletRequest servletRequest, UserAccountDetail userAccountDetail) {
        String verifyLink = ServerUtility.getSiteURL(servletRequest) + VERIFY_PATH
                + "?token=" + userAccountDetail.getVerificationCode();

        // Prepare email object
        EmailBusinessDto emailBusinessDto = EmailBusinessDto.builder()
                .mailType(MailType.USER)
                .userName(userAccountDetail.getUserAccount().getUsername())
                .firstName(userAccountDetail.getFirstName())
                .lastName(userAccountDetail.getLastName())
                .link(verifyLink)
                .build();
        List<String> toUsers = new ArrayList<>();
        toUsers.add(userAccountDetail.getEmail());
        List<String> ccUsers = new ArrayList<>();

        // Handle send email
        emailDataHandlerService.handleDataAndSendMail(emailBusinessDto, EmailTemplate.EMAIL_VERIFICATION,
                toUsers, ccUsers, EmailTemplate.EMAIL_VERIFICATION.baseSubject);
    }

    private void handleResetPasswordMailForUser(HttpServletRequest servletRequest, UserAccountDetail userAccountDetail) {
        String resetLink = ServerUtility.getSiteURL(servletRequest) + "/user/reset-password?token="
                + userAccountDetail.getResetToken();

        // Prepare email object
        EmailBusinessDto emailBusinessDto = EmailBusinessDto.builder()
                .mailType(MailType.USER)
                .userName(userAccountDetail.getUserAccount().getUsername())
                .firstName(userAccountDetail.getFirstName())
                .lastName(userAccountDetail.getLastName())
                .link(resetLink)
                .build();
        List<String> toUsers = new ArrayList<>();
        toUsers.add(userAccountDetail.getEmail());
        List<String> ccUsers = new ArrayList<>();

        // Handle send email
        emailDataHandlerService.handleDataAndSendMail(emailBusinessDto, EmailTemplate.EMAIL_RESET_PASSWORD,
                toUsers, ccUsers, EmailTemplate.EMAIL_RESET_PASSWORD.baseSubject);
    }

}
