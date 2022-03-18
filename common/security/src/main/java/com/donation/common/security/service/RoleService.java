package com.donation.common.security.service;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.management.entity.Role;
import com.donation.management.dto.RoleDto;

import java.util.List;

public interface RoleService {
    List<RoleDto> getRoles();

    Role findRole(RoleEnum roleEnum);
}
