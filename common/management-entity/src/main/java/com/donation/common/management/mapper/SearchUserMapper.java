package com.donation.common.management.mapper;

import com.donation.common.management.repository.projection.UserAccountProjection;
import com.donation.management.dto.UserAccountDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SearchUserMapper {

    @Mapping(target = "userAccount.username", source = "username")
    UserAccountDetailDto toDto(UserAccountProjection userAccount);
}
