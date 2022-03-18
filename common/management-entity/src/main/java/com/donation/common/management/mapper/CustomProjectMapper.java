package com.donation.common.management.mapper;

import com.donation.common.core.enums.TransactionStatus;
import com.donation.common.management.entity.Beneficiary;
import com.donation.common.management.entity.FollowerProject;
import com.donation.common.management.entity.Project;
import com.donation.common.management.repository.BeneficiaryRepository;
import com.donation.common.management.repository.DonationTimeRepository;
import com.donation.common.management.repository.projection.ProjectProjection;
import com.donation.management.dto.ProjectDto;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CustomProjectMapper {

    @Autowired
    private DonationTimeRepository donationTimeRepository;

    @Mappings({
            @Mapping(target = "projectContent", ignore = true),
            @Mapping(target = "countExtend", ignore = true),
            @Mapping(target = "beneficiary.beneficiaryPhoneNumber", ignore = true)
    })
    public abstract ProjectDto projectionToDto(ProjectProjection projectProjection);

    @Mappings({
            @Mapping(target = "projectId", source = "project.projectId"),
            @Mapping(target = "projectName", source = "project.projectName"),
            @Mapping(target = "status", source = "project.status"),
            @Mapping(target = "targetTime", source = "project.targetTime"),
            @Mapping(target = "targetMoney", source = "project.targetMoney"),
            @Mapping(target = "currentMoney", source = "project.currentMoney"),
            @Mapping(target = "beneficiary.beneficiaryName", source = "project.beneficiary.beneficiaryName")
    })
    public abstract ProjectDto followerToProjectDto(FollowerProject followerProject);

    public abstract ProjectDto projectToProjectDto(Project project);

    @Mappings({
            @Mapping(target = "projectContent", ignore = true),
            @Mapping(target = "projectName", ignore = true),
            @Mapping(target = "targetMoney", ignore = true),
    })
    public abstract ProjectDto projectToProjectTimeExtend(Project project);

    public ProjectDto projectToDtoDetail(Project project) {
        ProjectDto projectDto = projectToProjectDto(project);

        Long donatedCount = donationTimeRepository.countDonationTimeByProjectAndStatus(project.getProjectId(), TransactionStatus.COMPLETE.name());

        projectDto.setDonatedCount(donatedCount);

        // Set first file for file path
        projectDto.setFilePath(CollectionUtils.isNotEmpty(project.getProjectDocuments()) ? project.getProjectDocuments().get(0).getFilePath() : null);

        return projectDto;
    }

    public ProjectDto projectProjectionToDetail(ProjectProjection project) {
        ProjectDto projectDto = projectionToDto(project);

        Long donatedCount =
                donationTimeRepository.countDonationTimeByProjectAndStatus(project.getProjectId(),
                        TransactionStatus.COMPLETE.name());

        projectDto.setDonatedCount(donatedCount);

        return projectDto;
    }

}
