package com.donation.common.management.repository;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.management.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    @Query(value = "SELECT ur.role.roleCode " +
            "FROM UserRole ur " +
            "WHERE ur.userAccount.userId = :userId")
    List<RoleEnum> findRoleCodeByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT ur " +
            "FROM UserRole ur " +
            "WHERE ur.userAccount.userId = :userId")
    List<UserRole> findByUserId(@Param("userId") UUID userId);
}
