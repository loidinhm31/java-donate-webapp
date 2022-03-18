package com.donation.common.management.mapper;

import com.donation.common.management.entity.UserAccountDetail;
import com.donation.management.dto.UserAccountDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAccountDetailMapper {

    UserAccountDetail toEntity(UserAccountDetailDto userAccountDto);

    UserAccountDetailDto toDto(UserAccountDetail userAccount);
}
