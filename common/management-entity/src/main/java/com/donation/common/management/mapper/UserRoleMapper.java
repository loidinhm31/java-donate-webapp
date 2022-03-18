package com.donation.common.management.mapper;

import com.donation.common.management.entity.UserRole;
import com.donation.management.dto.UserRoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRoleMapper {
    @Mapping(target = "role", ignore = true)
    UserRole toEntity(UserRoleDto userRoleDto);

    @Mappings({
            @Mapping(target = "userId", source = "userAccount.userId")
    })
    UserRoleDto toDto(UserRole userRole);
}
