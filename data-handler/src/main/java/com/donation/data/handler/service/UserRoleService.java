package com.donation.data.handler.service;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.management.dto.UserAccountDto;
import com.donation.management.dto.UserRoleDto;
import com.donation.management.model.ProvideRoleRequest;

import java.util.List;
import java.util.UUID;

public interface UserRoleService {
    List<UserRoleDto> getAllRolesForUserWithCheck(UUID userId);

    UserAccountDto updateRole(UserAccountDto userAccountDto) throws InvalidMessageException;

    void updatePermission(ProvideRoleRequest roleRequest) throws InvalidMessageException;

    void changeActive(String userId, Boolean isActive) throws InvalidMessageException;
}
