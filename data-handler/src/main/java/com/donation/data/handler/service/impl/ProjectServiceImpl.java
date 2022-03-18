package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.Beneficiary;
import com.donation.common.management.entity.Project;
import com.donation.common.management.entity.ProjectDocument;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.mapper.CustomProjectMapper;
import com.donation.common.management.mapper.ProjectMapper;
import com.donation.common.management.repository.ProjectDocumentRepository;
import com.donation.common.management.repository.ProjectRepository;
import com.donation.common.management.repository.projection.ProjectProjection;
import com.donation.common.security.SecurityUtils;
import com.donation.data.handler.service.*;
import com.donation.management.dto.ProjectDto;
import com.donation.management.dto.ProjectRequestDto;
import com.donation.management.model.ProjectSearchFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.donation.common.core.constant.MessageConstants.*;

@Slf4j
@Service
public class ProjectServiceImpl extends BaseInitiatedServiceImpl
        implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectDocumentRepository projectDocumentRepository;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private GenerateIdService generateIdService;

    @Autowired
    private FollowerProjectService followerProjectService;

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private BlobStoreService blobStoreService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CustomProjectMapper customProjectMapper;

    @Override
    public Page<ProjectProjection> getProjectsForAdmin(ProjectSearchFilter projectSearchFilter) {
        Page<ProjectProjection> projects = projectRepository.searchProjects(projectSearchFilter);

        for (ProjectProjection projectProjection : projects) {
            List<ProjectDocument> projectDocuments = projectDocumentRepository.findByProjectId(projectProjection.getProjectId());
            // Set first file for file path
            projectProjection.setFilePath(CollectionUtils.isNotEmpty(projectDocuments) ? projectDocuments.get(0).getFilePath() : null);
        }

        return projects;
    }

    @Override
    public Page<ProjectDto> getProjectsForUser(ProjectSearchFilter projectSearchFilter) {
        Page<ProjectProjection> projects = projectRepository.searchProjects(projectSearchFilter);

        Page<ProjectDto> projectDtos = projects.map(customProjectMapper::projectProjectionToDetail);

        for (ProjectDto projectDto : projectDtos) {
            List<ProjectDocument> projectDocuments = projectDocumentRepository.findByProjectId(projectDto.getProjectId());
            // Set first file for file path
            projectDto.setFilePath(CollectionUtils.isNotEmpty(projectDocuments) ? projectDocuments.get(0).getFilePath() : null);
        }
        return projectDtos;
    }

    @Override
    public List<ProjectDto> getProjectCodes() {
        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .map(p -> {
                    ProjectDto projectDto = ProjectDto.builder()
                            .projectId(p.getProjectId())
                            .projectName(p.getProjectName())
                            .build();
                    return projectDto;
                })
                .sorted(Comparator.comparing(ProjectDto::getProjectId))
                .collect(Collectors.toList());
    }

    @Override
    public void saveOrUpdate(ProjectRequestDto projectRequestDto, MultipartFile multipartFile) throws Exception {

        if (StringUtils.isEmpty(projectRequestDto.getProjectId())
                && (Objects.isNull(projectRequestDto.getStartTime())
                    || Objects.isNull(projectRequestDto.getTargetTime()))) {
            throw new InvalidMessageException("Date cannot be empty");
        }

        if (Objects.nonNull(projectRequestDto.getStartTime())
                && Objects.nonNull(projectRequestDto.getTargetTime())
                && projectRequestDto.getStartTime().after(projectRequestDto.getTargetTime())) {
            throw new InvalidMessageException("Start Date cannot greater than Target Date");
        }


        if (StringUtils.isEmpty(projectRequestDto.getBeneficiaryId())) {
            throw new InvalidMessageException("Beneficiary invalid");
        }

        if (Objects.isNull(projectRequestDto.getTargetMoney())) {
            throw new InvalidMessageException("Target money cannot be empty");
        }

        if (StringUtils.isEmpty(projectRequestDto.getProjectContent())) {
            throw new InvalidMessageException("Story cannot be empty");
        }

        Beneficiary beneficiary = beneficiaryService.getBeneficiary(projectRequestDto.getBeneficiaryId());
        if (Objects.isNull(beneficiary)) {
            throw new InvalidMessageException("Cannot find beneficiary");
        }

        // Define identifier
        String identifier = SecurityUtils.getUserIdentifier();
        UserAccountDetail userAccountDetail = userAccountDetailsService.findUserByIdentifier(identifier);

        if (checkPermissionWithRole()) {
            Project project;
            if (Objects.isNull(projectRequestDto.getProjectId())) {
                log.info("Creating a new project");
                project = projectMapper.requestToEntity(projectRequestDto);

                // Generate project id
                project.setProjectId(generateIdService.generateProjectId(project.getProjectName()));
                project.setStatus(ProjectStatusEnum.CREATED);
                project.setCreator(userAccountDetail);
                project.setCurrentMoney(0F);    //  Default 0 for created
                project.setCreatedBy(identifier);
                project.setUpdatedBy(identifier);
            } else {
                Optional<Project> optionalProject = projectRepository.findById(projectRequestDto.getProjectId());
                project = optionalProject.orElseThrow(DataProcessNotFoundException::new);
                log.info("Updating existing project: " + project.getProjectId());

                // Only change status for ENDED to CLOSED
                if (project.getStatus().equals(ProjectStatusEnum.ENDED)
                        && Objects.nonNull(projectRequestDto.getStatus())
                        && projectRequestDto.getStatus().equals(ProjectStatusEnum.CLOSED)) {
                    project.setStatus(ProjectStatusEnum.CLOSED);
                }

                project.setProjectName(projectRequestDto.getProjectName());
                project.setProjectSummary(projectRequestDto.getProjectSummary());
                project.setProjectContent(projectRequestDto.getProjectContent());
                project.setTargetMoney(projectRequestDto.getTargetMoney());
                project.setUpdatedBy(identifier);

                if (project.getStatus().equals(ProjectStatusEnum.CREATED)) {
                    project.setStartTime(projectRequestDto.getStartTime());
                }
            }

            project.setBeneficiary(beneficiary);
            Project savedProject = projectRepository.save(project);

            // Upload file
            if (Objects.nonNull(multipartFile)
                    && !multipartFile.isEmpty()) {
                log.info("File not null, uploading it for project");
                String filePath = blobStoreService.uploadFile(multipartFile);
                if (StringUtils.isNotEmpty(filePath)) {
                    ProjectDocument projectDocument;

                    if (CollectionUtils.isNotEmpty(savedProject.getProjectDocuments())) { // Check for update first file
                        projectDocument = projectDocumentRepository.findFirstByProjectId(savedProject.getProjectId());
                        // Delete old file
                        CompletableFuture
                                .runAsync(() ->
                                        blobStoreService.deleteFile(projectDocument.getFilePath()));

                        log.info("Saving thumbnail for: " + savedProject.getProjectId());
                        projectDocument.setFilePath(filePath);
                    } else {    // Create a new thumbnail
                        log.info("Building new file document...");
                        // Create file object
                        projectDocument = ProjectDocument.builder()
                                .project(savedProject)
                                .filePath(filePath)
                                .build();
                    }
                    projectDocumentRepository.save(projectDocument);
                    log.info("Saved thumbnail for the project");
                }
            }
        } else throw new InvalidMessageException("User has not permission");
    }

    @Override
    public ProjectDto getProject(String projectId) throws InvalidMessageException {
        Optional<Project> optionalProject = projectRepository.findById(projectId);

        if (optionalProject.isEmpty()) {
            throw new InvalidMessageException("Project does not exists");
        }

        return customProjectMapper.projectToDtoDetail(optionalProject.orElse(null));
    }

    @Override
    public Project findProjectByProjectIdAndStatus(String projectId, ProjectStatusEnum projectStatusEnum) {
        return projectRepository
                .findByProjectIdAndStatus(projectId, projectStatusEnum.name());
    }

    @Override
    public void updateProject(Project project) {
        projectRepository.save(project);
    }

    @Override
    public void extendTimeProject(ProjectDto projectDto) throws DataProcessNotFoundException, InvalidMessageException {

        if (Objects.isNull(projectDto.getTargetTime())) {
            throw new InvalidMessageException(INVALID_TIME_EXCEPTION);
        }

        if (!checkPermissionWithRole()) {
            throw new DataProcessNotFoundException(AUTHORIZE_EXCEPTION);
        }

        Optional<Project> optionalProject = projectRepository.findById(projectDto.getProjectId());

        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();

            // Update for non-close project
            if (!project.getStatus().equals(ProjectStatusEnum.CLOSED)) {
                Calendar calBase = Calendar.getInstance();
                calBase.setTime(project.getTargetTime());
                Calendar calUpdate = Calendar.getInstance();
                calUpdate.setTime(projectDto.getTargetTime());

                // Check time update
                if (!((calBase.get(Calendar.YEAR) == calUpdate.get(Calendar.YEAR))
                            && (calBase.get(Calendar.MONTH) == calUpdate.get(Calendar.MONTH))
                            && (calBase.get(Calendar.DAY_OF_MONTH) == calUpdate.get(Calendar.DAY_OF_MONTH)))
                        && calUpdate.getTime().after(calBase.getTime())
                        && (Objects.isNull(project.getCountExtend())
                            || project.getCountExtend() < 3)) {
                    project.setTargetTime(projectDto.getTargetTime());

                    // Increase count
                    if (Objects.nonNull(project.getCountExtend())) {
                        project.setCountExtend(project.getCountExtend() + 1);
                    } else {
                        project.setCountExtend(1);
                    }

                    // Back status to In-progress
                    project.setStatus(ProjectStatusEnum.IN_PROGRESS);

                    // Update updator
                    project.setUpdatedBy(SecurityUtils.getUserIdentifier());

                    projectRepository.save(project);
                } else throw new InvalidMessageException("Invalid date exception");
            } else throw new InvalidMessageException(PROJECT_STATUS_EXCEPTION);
        }
    }

    @Override
    public Project findProject(String projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        return project.orElse(null);
    }

    @Override
    public ProjectDto getProjectForTimeExtend(String projectId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        return customProjectMapper.projectToProjectTimeExtend(optionalProject.orElse(null));
    }

    @Override
    public void deleteInitProject(String projectId) throws DataProcessNotFoundException {
        Optional<Project> optionalProject = projectRepository.findById(projectId);

        if (!checkPermissionWithRole()) {
            throw new DataProcessNotFoundException(AUTHORIZE_EXCEPTION);
        }

        if (optionalProject.isPresent()) {

            Project project = optionalProject.get();
            if (project.getStatus().equals(ProjectStatusEnum.CREATED)) {
                // Delete follower
                followerProjectService.deleteFollowersOfProject(projectId);

                project.getFollowerProjects().clear();

                projectDocumentRepository.deleteAll(project.getProjectDocuments());

                project.getProjectDocuments().clear();

                projectRepository.save(project);    // work-around for jpa bug
                projectRepository.delete(project);
            }
        }
    }

    @Override
    public List<ProjectDto> getProjectsLoader() {
        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .filter(p -> checkValidProjectStatus(p.getStatus()))
                .map(p -> ProjectDto.builder()
                        .projectId(p.getProjectId())
                        .projectName(p.getProjectName())
                        .build())
                .sorted(Comparator.comparing(ProjectDto::getProjectId))
                .collect(Collectors.toList());
    }
}
