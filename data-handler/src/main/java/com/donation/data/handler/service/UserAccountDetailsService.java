package com.donation.data.handler.service;

import com.donation.common.core.enums.AuthenticationType;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.UserAccount;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.management.dto.UserAccountDetailDto;
import com.donation.management.dto.UserRequestDto;
import com.donation.management.model.UserFilter;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public interface UserAccountDetailsService {

    void updateAuthenticationType(UserAccountDetail userDetails, AuthenticationType authenticationType);

    boolean isUsernameUnique(String username);

    boolean isEmailUnique(String email);

    void createUser(HttpServletRequest servletRequest, UserRequestDto userRequestDto) throws InvalidMessageException;

    boolean verify(String code);

    UserAccountDetailDto getUser(UUID userId) throws InvalidMessageException;

    Page<UserAccountDetailDto> getUsers(UserFilter userFilter);

    void updateResetPasswordToken(HttpServletRequest servletRequest, String email) throws InvalidMessageException;

    UserAccountDetailDto getByResetPasswordToken(String token);

    void updatePassword(String token, String password) throws InvalidMessageException;

    UserAccountDetailDto updateUserAccount(UserAccountDetailDto userAccountDetailDto) throws InvalidMessageException;

    UserAccountDetail findUserAccountDetailByEmail(String email);

    void createUserByOAuthLogin(String name, String email, AuthenticationType authenticationType);

    UserAccountDetailDto getUserByIdentifier(String identifier) throws InvalidMessageException;

    UserAccountDetail findUserByIdentifier(String identifier);
}
