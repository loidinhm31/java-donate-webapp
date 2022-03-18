package com.donation.data.handler.service.impl;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.FollowerId;
import com.donation.common.management.entity.FollowerProject;
import com.donation.common.management.entity.Project;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.mapper.CustomProjectMapper;
import com.donation.common.management.repository.FollowerProjectRepository;
import com.donation.common.security.SecurityUtils;
import com.donation.data.handler.service.FollowerProjectService;
import com.donation.data.handler.service.ProjectService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.management.dto.ProjectDto;
import com.donation.management.model.ProjectSearchFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.donation.common.core.constant.MessageConstants.AUTHORIZE_EXCEPTION;

@Service
public class FollowerProjectServiceImpl implements FollowerProjectService {

    @Autowired
    private FollowerProjectRepository followProjectRepository;

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CustomProjectMapper customProjectMapper;

    @Override
    public boolean followProject(String projectId, Boolean follow) throws InvalidMessageException {

        String identifier = SecurityUtils.getUserIdentifier();

        if (StringUtils.isNotEmpty(identifier)) {
            UserAccountDetail userAccountDetail =
                    userAccountDetailsService.findUserByIdentifier(identifier);

            if (follow) {
                Project project = projectService.findProject(projectId);

                FollowerId followerId = new FollowerId();
                followerId.setProjectId(projectId);
                followerId.setUserId(userAccountDetail.getUserId());

                FollowerProject followerProject = FollowerProject.builder()
                        .followerId(followerId)
                        .userAccountDetail(userAccountDetail)
                        .project(project)
                        .build();

                FollowerProject savedFollower = followProjectRepository.save(followerProject);

                return Boolean.TRUE;
            } else {
                FollowerProject followerProject =
                        followProjectRepository.findByProjectIdAndUserId(projectId, userAccountDetail.getUserId());

                followProjectRepository.delete(followerProject);

                return Boolean.FALSE;
            }
        } else throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
    }

    @Override
    public Boolean checkFollowByProject(String projectId, String userIdentifier) {
        FollowerProject followerProject =
                followProjectRepository.findByProjectIdAndUserIdentifier(projectId, userIdentifier);

        if (Objects.nonNull(followerProject)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Page<ProjectDto> getProjectByFollower(ProjectSearchFilter projectSearchFilter) {
        String identifier = SecurityUtils.getUserIdentifier();

        UserAccountDetail userAccountDetail = userAccountDetailsService.findUserByIdentifier(identifier);

        Pageable pageable = PageRequest.of(projectSearchFilter.getStartPage(), projectSearchFilter.getPageSize());

        Page<FollowerProject> followerProjects =
                followProjectRepository.findByUserId(userAccountDetail.getUserId(), pageable);

        return followerProjects.map(customProjectMapper::followerToProjectDto);
    }


    @Override
    public void deleteFollowersOfProject(String projectId) {
        List<FollowerProject> followerProjects = followProjectRepository.findAllByProjectId(projectId);

        if (CollectionUtils.isNotEmpty(followerProjects)) {
            followProjectRepository.deleteAll(followerProjects);
        }
    }

}
