package com.donation.data.handler.service;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.FollowerProject;
import com.donation.management.dto.ProjectDto;
import com.donation.management.model.ProjectSearchFilter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface FollowerProjectService {
    boolean followProject(String projectId, Boolean follow) throws InvalidMessageException;

    Boolean checkFollowByProject(String projectId, String userIdentifier);

    Page<ProjectDto> getProjectByFollower(ProjectSearchFilter projectSearchFilter);

    void deleteFollowersOfProject(String projectId);
}
