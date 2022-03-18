package com.donation.common.management.mapper;

import com.donation.common.management.entity.Project;
import com.donation.management.dto.ProjectDto;
import com.donation.management.dto.ProjectRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    @Mapping(target = "beneficiary", ignore = true)
    Project requestToEntity(ProjectRequestDto projectRequestDto);

}
