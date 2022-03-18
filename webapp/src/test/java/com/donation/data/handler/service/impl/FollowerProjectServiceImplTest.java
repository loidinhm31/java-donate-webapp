package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.*;
import com.donation.common.management.mapper.CustomProjectMapper;
import com.donation.common.management.repository.FollowerProjectRepository;
import com.donation.common.security.CustomUserDetail;
import com.donation.data.handler.service.FollowerProjectService;
import com.donation.data.handler.service.ProjectService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.impl.FollowerProjectServiceImpl;
import com.donation.data.handler.service.impl.ProjectServiceImpl;
import com.donation.data.handler.service.impl.UserAccountDetailsServiceImpl;
import com.donation.management.dto.ProjectDto;
import com.donation.management.model.ProjectSearchFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FollowerProjectServiceImplTest {
    @InjectMocks
    private FollowerProjectService followerProjectServiceTest = new FollowerProjectServiceImpl();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Mock
    private ProjectService projectService = new ProjectServiceImpl();

    @Mock
    private FollowerProjectRepository followerProjectRepository;

    @Spy
    private CustomProjectMapper customProjectMapper = Mappers.getMapper(CustomProjectMapper.class);

    @Test
    public void testFollowProject() throws InvalidMessageException {
        UserAccountDetail userAccountDetail = buildForUser();

        String projectId = "NC-2100001";
        Project project = Project.builder()
                .projectId(projectId)
                .build();

        when(projectService.findProject(projectId))
                .thenReturn(project);

        // Call the function to test
        followerProjectServiceTest.followProject(projectId, Boolean.TRUE);

        ArgumentCaptor<FollowerProject> followerProjectArgumentCaptor =
                ArgumentCaptor.forClass(FollowerProject.class);

        verify(followerProjectRepository).save(followerProjectArgumentCaptor.capture());

        FollowerProject followerProject = followerProjectArgumentCaptor.getValue();

        assertThat(followerProject).isNotNull();
    }

    @Test
    public void testUnfollowProject() throws InvalidMessageException {
        UserAccountDetail userAccountDetail = buildForUser();

        String projectId = "NC-2100001";
        FollowerProject followerProject = FollowerProject.builder()
                .project(Project.builder()
                        .projectId(projectId)
                        .build())
                .build();

        when(followerProjectRepository.findByProjectIdAndUserId(projectId, userAccountDetail.getUserId()))
                .thenReturn(followerProject);

        // Call the function to test
        followerProjectServiceTest.followProject(projectId, Boolean.FALSE);

        ArgumentCaptor<FollowerProject> followerProjectArgumentCaptor =
                ArgumentCaptor.forClass(FollowerProject.class);

        verify(followerProjectRepository).delete(followerProjectArgumentCaptor.capture());
    }

    @Test
    public void testCheckFollowByProject() {
        String projectId = "NC-2100001";
        UUID userId = UUID.randomUUID();
        String userIdentifier = "user";

        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .username(userIdentifier)
                .userPassword("user")
                .userRoles(List.of(UserRole.builder()
                        .role(Role.builder()
                                .roleId(UUID.randomUUID())
                                .roleCode(RoleEnum.CLIENT)
                                .roleName(RoleEnum.CLIENT.getRoleName())
                                .build())
                        .build()))
                .build();

        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(userId)
                .userAccount(userAccount)
                .build();
        userAccount.setUserAccountDetail(userAccountDetail);


        when(followerProjectRepository.findByProjectIdAndUserIdentifier(projectId, "user"))
                .thenReturn(FollowerProject.builder()
                        .userAccountDetail(userAccountDetail)
                        .project(Project.builder()
                                .projectId(projectId)
                                .build())
                        .build());

        // Call the function to test
        Boolean isFollow = followerProjectServiceTest.checkFollowByProject(projectId, "user");
        assertThat(isFollow).isTrue();
    }

    @Test
    public void testGetProjectByFollower() {
        UserAccountDetail userAccountDetail = buildForUser();

        ProjectSearchFilter projectSearchFilter = ProjectSearchFilter.builder()
                .startPage(0)
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();

        Pageable pageable = PageRequest.of(projectSearchFilter.getStartPage(), projectSearchFilter.getPageSize());


        Page<FollowerProject> followerProjects = new PageImpl<>(
                List.of(FollowerProject.builder()
                        .build())
        );

        when(followerProjectRepository.findByUserId(userAccountDetail.getUserId(), pageable))
                .thenReturn(followerProjects);

        // Call the function to test
        Page<ProjectDto> projects =
                followerProjectServiceTest.getProjectByFollower(projectSearchFilter);

        assertThat(projects.getTotalElements()).isEqualTo(followerProjects.getTotalElements());
    }

    @Test
    public void testDeleteFollowersOfProject() {
        String projectId = "NC-2100001";

        Project project = Project.builder()
                .projectId(projectId)
                .build();

        when(followerProjectRepository.findAllByProjectId(projectId))
                .thenReturn(List.of(
                        FollowerProject.builder()
                            .project(project)
                            .build()
                ));

        // Call the function to test
        followerProjectServiceTest.deleteFollowersOfProject(projectId);

        ArgumentCaptor<List<FollowerProject>> listArgumentCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(followerProjectRepository)
                .deleteAll(listArgumentCaptor.capture());

        List<FollowerProject> listArgumentCaptorValue = listArgumentCaptor.getValue();

    }

    private UserAccountDetail buildForUser() {
        // Custom authentication
        String userIdentifier = "user";
        UUID userId = UUID.randomUUID();

        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .username(userIdentifier)
                .userPassword("user")
                .userRoles(List.of(UserRole.builder()
                        .role(Role.builder()
                                .roleId(UUID.randomUUID())
                                .roleCode(RoleEnum.CLIENT)
                                .roleName(RoleEnum.CLIENT.getRoleName())
                                .build())
                        .build()))
                .build();

        UserAccountDetail userAccountDetail = UserAccountDetail.builder()
                .userId(userId)
                .userAccount(userAccount)
                .build();
        userAccount.setUserAccountDetail(userAccountDetail);

        CustomUserDetail customUserDetail =
                new CustomUserDetail(userAccount);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(customUserDetail);

        when(userAccountDetailsService.findUserByIdentifier(userIdentifier))
                .thenReturn(userAccountDetail);

        return userAccountDetail;
    }
}
