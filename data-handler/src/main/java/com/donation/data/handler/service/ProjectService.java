package com.donation.data.handler.service;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.Project;
import com.donation.common.management.repository.projection.ProjectProjection;
import com.donation.management.dto.ProjectDto;
import com.donation.management.dto.ProjectRequestDto;
import com.donation.management.model.ProjectSearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService {

    Page<ProjectProjection> getProjectsForAdmin(ProjectSearchFilter projectSearchFilter);

    Page<ProjectDto> getProjectsForUser(ProjectSearchFilter projectSearchFilter);

    List<ProjectDto> getProjectCodes();

    void saveOrUpdate(ProjectRequestDto projectRequestDto, MultipartFile multipartFile) throws Exception;

    /**
     * Get detail information for specific project
     *
     * @param projectId
     * @return
     * @throws InvalidMessageException
     */
    ProjectDto getProject(String projectId) throws InvalidMessageException;

    Project findProjectByProjectIdAndStatus(String projectId, ProjectStatusEnum projectStatusEnum);

    void updateProject(Project project);

    void extendTimeProject(ProjectDto projectDto) throws DataProcessNotFoundException, InvalidMessageException;

    Project findProject(String projectId);

    ProjectDto getProjectForTimeExtend(String projectId);

    void deleteInitProject(String projectId) throws DataProcessNotFoundException;

    /**
     * Load list of active projects
     *
     * @return
     */
    List<ProjectDto> getProjectsLoader();
}
