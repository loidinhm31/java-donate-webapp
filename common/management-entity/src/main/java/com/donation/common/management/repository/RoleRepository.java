package com.donation.common.management.repository;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.management.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByRoleCode(RoleEnum roleCode);

    List<Role> findAllByRoleCodeIn(List<RoleEnum> roleCodes);

}
