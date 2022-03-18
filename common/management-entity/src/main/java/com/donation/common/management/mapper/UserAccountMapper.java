package com.donation.common.management.mapper;

import com.donation.common.management.entity.UserAccount;
import com.donation.management.dto.UserAccountDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAccountMapper {
    @Mapping(target = "userRoles", ignore = true)
    UserAccount toEntity(UserAccountDto userAccountDto);

    UserAccountDto toDto(UserAccount userAccount);
}
