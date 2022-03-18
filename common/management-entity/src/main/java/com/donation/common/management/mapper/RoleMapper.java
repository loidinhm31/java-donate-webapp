package com.donation.common.management.mapper;

import com.donation.common.management.entity.Role;
import com.donation.management.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    Role toEntity(RoleDto roleDto);
    RoleDto toDto(Role role);
}
