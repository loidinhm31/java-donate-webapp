package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.*;
import com.donation.common.core.exception.DataProcessNotFoundException;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.*;
import com.donation.common.management.mapper.CustomProjectMapper;
import com.donation.common.management.mapper.ProjectMapper;
import com.donation.common.management.repository.BeneficiaryRepository;
import com.donation.common.management.repository.DonationTimeRepository;
import com.donation.common.management.repository.ProjectDocumentRepository;
import com.donation.common.management.repository.ProjectRepository;
import com.donation.common.management.repository.projection.ProjectProjection;
import com.donation.common.security.CustomUserDetail;
import com.donation.data.handler.service.*;
import com.donation.data.handler.service.impl.*;
import com.donation.management.dto.ProjectDto;
import com.donation.management.dto.ProjectRequestDto;
import com.donation.management.model.ProjectSearchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest {
    @InjectMocks
    private ProjectService projectServiceTest = new ProjectServiceImpl();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Mock
    private BeneficiaryService beneficiaryService = new BeneficiaryServiceImpl();

    @Mock
    private GenerateIdService generateIdService = new GenerateIdServiceImpl();

    @Mock
    private BlobStoreService blobStoreService = new BlobStoreServiceImpl();

    @Mock
    private FollowerProjectService followerProjectService = new FollowerProjectServiceImpl();

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DonationTimeRepository donationTimeRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private ProjectDocumentRepository projectDocumentRepository;

    @Spy
    private CustomProjectMapper customProjectMapper = Mappers.getMapper(CustomProjectMapper.class);

    @Spy
    private ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetProjectsForAdmin() {
        ProjectSearchFilter projectSearchFilter = ProjectSearchFilter.builder()
                .startPage(0)
                .pageSize(DEFAULT_PAGE_SIZE)
                .sortBy(ProjectSearchColumn.UPDATED_AT)
                .sortType(SortType.DESC)
                .build();

        Page<ProjectProjection> projects = new PageImpl<>(
                List.of(ProjectProjection.builder()
                        .projectId("NC-210001")
                        .build())
        );

        when(projectRepository.searchProjects(projectSearchFilter))
                .thenReturn(projects);

        // Call the function to test
        Page<ProjectProjection> projectProjections =
                projectServiceTest.getProjectsForAdmin(projectSearchFilter);

        assertThat(projectProjections).isNotNull();
    }

    @Test
    public void testGetProjectsForUser() {
        ProjectSearchFilter projectSearchFilter = ProjectSearchFilter.builder()
                .startPage(0)
                .pageSize(DEFAULT_PAGE_SIZE)
                .sortBy(ProjectSearchColumn.UPDATED_AT)
                .sortType(SortType.DESC)
                .build();

        Page<ProjectProjection> projects = new PageImpl<>(
                List.of(ProjectProjection.builder()
                        .projectId("NC-2100001")
                        .build())
        );

        ReflectionTestUtils.setField(customProjectMapper, "donationTimeRepository", donationTimeRepository);


        when(projectRepository.searchProjects(projectSearchFilter))
                .thenReturn(projects);

        // Call the function to test
        Page<ProjectDto> projectProjections =
                projectServiceTest.getProjectsForUser(projectSearchFilter);
    }

    @Test
    public void testGetProjectCodes() {
        Project projectOne = Project.builder()
                .projectId("NC-2100001")
                .projectName("NC ONE")
                .build();

        when(projectRepository.findAll()).thenReturn(List.of(projectOne));

        // Call the function to test
        List<ProjectDto> projectCodes = projectServiceTest.getProjectCodes();

        assertThat(projectCodes).hasSize(1);
    }

    @Test
    public void testCreateProject() throws Exception {
        buildForAdmin();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        String beneficiaryId = "AB-2100001";
        ProjectRequestDto projectRequestDto = ProjectRequestDto.builder()
                .targetMoney(20000000F)
                .startTime(dateFormat.parse("01-01-2022"))
                .targetTime(dateFormat.parse("15-01-2022"))
                .projectContent("TEST")
                .beneficiaryId(beneficiaryId)
                .build();

        // Build multipart
        MultipartFile multipartFile =
                new MockMultipartFile("file", "orig", null, "bar".getBytes());

        when(beneficiaryService.getBeneficiary(beneficiaryId))
                .thenReturn(Beneficiary.builder()
                        .beneficiaryId(beneficiaryId)
                        .beneficiaryName("Annie Batch")
                        .build());

        // Call the function to test
        projectServiceTest.saveOrUpdate(projectRequestDto, multipartFile);

        ArgumentCaptor<Project> projectArgumentCaptor =
                ArgumentCaptor.forClass(Project.class);

        verify(projectRepository)
                .save(projectArgumentCaptor.capture());

        Project capturedProject = projectArgumentCaptor.getValue();

        assertThat(capturedProject.getStatus())
                .isEqualTo(ProjectStatusEnum.CREATED);

    }

    @Test
    public void testGetProject() throws InvalidMessageException {
        String projectId = "NC-2100001";

        Optional<Project> projectOptional = Optional.ofNullable(Project.builder()
                .projectId(projectId)
                .build());

        when(projectRepository.findById(projectId))
                .thenReturn(projectOptional);

        // Reflect repository in mapper
        ReflectionTestUtils.setField(customProjectMapper, "donationTimeRepository", donationTimeRepository);
        when(donationTimeRepository.countDonationTimeByProjectAndStatus(projectId, TransactionStatus.COMPLETE.name()))
                .thenReturn(10L);

        // Call the function to test
        ProjectDto projectDto = projectServiceTest.getProject(projectId);

        assertThat(projectDto).isNotNull();
    }

    @Test
    public void testFindProjectByProjectIdAndStatus() {
        String projectId = "NC-2100001";

        Project builtProject = Project.builder()
                .projectId(projectId)
                .status(ProjectStatusEnum.IN_PROGRESS)
                .build();

        when(projectRepository.findByProjectIdAndStatus(projectId, builtProject.getStatus().name()))
                .thenReturn(builtProject);


        // Call the function to test
        Project project = projectServiceTest
                .findProjectByProjectIdAndStatus(projectId, ProjectStatusEnum.IN_PROGRESS);

        assertThat(project).isEqualTo(builtProject);
    }

    @Test
    public void testExtendTime() throws ParseException, InvalidMessageException, DataProcessNotFoundException {
        buildForAdmin();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        String projectId = "NC-2100001";
        ProjectDto projectDto = ProjectDto.builder()
                .projectId(projectId)
                .projectName("CHANGE NAME")
                .targetTime(simpleDateFormat.parse("30-12-2022"))
                .build();

        Optional<Project> projectOptional = Optional.ofNullable(Project.builder()
                .projectId(projectId)
                .projectName("CURRENT NAME")
                .targetTime(simpleDateFormat.parse("25-12-2022"))
                .status(ProjectStatusEnum.CREATED)
                .build());

        when(projectRepository.findById(projectId))
                .thenReturn(projectOptional);

        // Call the function to test
        projectServiceTest.extendTimeProject(projectDto);

        ArgumentCaptor<Project> projectArgumentCaptor = ArgumentCaptor.forClass(Project.class);

        verify(projectRepository).save(projectArgumentCaptor.capture());

        Project savedProject = projectArgumentCaptor.getValue();

        assertThat(savedProject.getCountExtend()).isEqualTo(1);
    }

    @Test
    public void testSaveOrUpdateProject() throws Exception {
        buildForAdmin();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        String projectId = "NC-2100001";
        String beneficiaryId = "AA-2100001";
        ProjectRequestDto projectRequestDto = ProjectRequestDto.builder()
                .projectId(projectId)
                .projectName("CHANGE NAME")
                .targetMoney(20000000F)
                .startTime(dateFormat.parse("01-01-2022"))
                .targetTime(dateFormat.parse("15-01-2022"))
                .projectContent("TEST")
                .beneficiaryId(beneficiaryId)
                .build();

        Optional<Project> projectOptional = Optional.ofNullable(Project.builder()
                .projectId(projectId)
                .projectName("CURRENT NAME")
                .status(ProjectStatusEnum.CREATED)
                .build());

        when(projectRepository.findById(projectId))
                .thenReturn(projectOptional);

        when(beneficiaryService.getBeneficiary(beneficiaryId))
                .thenReturn(Beneficiary.builder()
                        .beneficiaryId(beneficiaryId)
                        .build());

        // Build multipart
        MultipartFile multipartFile =
                new MockMultipartFile("file", "orig", null, "bar".getBytes());

        // Call the function to test
        projectServiceTest.saveOrUpdate(projectRequestDto, multipartFile);

        ArgumentCaptor<Project> projectArgumentCaptor = ArgumentCaptor.forClass(Project.class);

        verify(projectRepository)
                .save(projectArgumentCaptor.capture());

        Project savedProject = projectArgumentCaptor.getValue();

        assertThat(savedProject.getProjectName()).isEqualTo("CHANGE NAME");
        assertThat(savedProject.getBeneficiary().getBeneficiaryId()).isEqualTo(beneficiaryId);
    }

    @Test
    public void testFindProject() {
        String projectId = "NC-2100001";
        Optional<Project> projectOptional = Optional.ofNullable(Project.builder()
                .projectId(projectId)
                .projectName("CURRENT NAME")
                .status(ProjectStatusEnum.CREATED)
                .build());

        when(projectRepository.findById(projectId))
                .thenReturn(projectOptional);

        // Call the function to test
        Project project = projectServiceTest.findProject(projectId);

        assertThat(project).isNotNull();
    }

    @Test
    public void testGetTimeForProject() {
        String projectId = "NC-2100001";
        Optional<Project> projectOptional = Optional.ofNullable(Project.builder()
                .projectId(projectId)
                .projectName("CURRENT NAME")
                .status(ProjectStatusEnum.CREATED)
                .build());

        when(projectRepository.findById(projectId))
                .thenReturn(projectOptional);

        // Call the function to test
        ProjectDto projectDto = projectServiceTest.getProjectForTimeExtend(projectId);

        assertThat(projectDto).isNotNull();
    }

    @Test
    public void testDeleteInitProject() throws DataProcessNotFoundException {
        buildForAdmin();

        List<FollowerProject> followerProjects = new ArrayList<>();
        followerProjects.add(FollowerProject.builder().build());

        List<ProjectDocument> projectDocuments = new ArrayList<>();
        projectDocuments.add(ProjectDocument.builder().build());

        String projectId = "NC-2100001";
        Optional<Project> projectOptional = Optional.ofNullable(Project.builder()
                .projectId(projectId)
                .projectName("CURRENT NAME")
                .status(ProjectStatusEnum.CREATED)
                .followerProjects(followerProjects)
                .projectDocuments(projectDocuments)
                .build());

        when(projectRepository.findById(projectId))
                .thenReturn(projectOptional);

        // Call the function to test
        projectServiceTest.deleteInitProject(projectId);

        ArgumentCaptor<Project> projectArgumentCaptor = ArgumentCaptor.forClass(Project.class);

        verify(projectRepository).save(projectArgumentCaptor.capture());

        verify(projectRepository).delete(projectArgumentCaptor.capture());
    }

    @Test
    public void testGetProjectsLoader() {

        Project projectOne = Project.builder()
                .projectId("1")
                .projectName("A")
                .status(ProjectStatusEnum.IN_PROGRESS)
                .build();

        Project projectTwo = Project.builder()
                .projectId("2")
                .projectName("B")
                .status(ProjectStatusEnum.ENDED)
                .build();

        when(projectRepository.findAll())
                .thenReturn(List.of(projectOne, projectTwo));

        // Call the function to test
        List<ProjectDto> projectDtos = projectServiceTest.getProjectsLoader();

        assertThat(projectDtos)
                .hasSize(1)
                .isSortedAccordingTo(Comparator.comparing(ProjectDto::getProjectId));
    }

    private UserAccountDetail buildForAdmin() {
        // Custom authentication
        String userIdentifier = "root";
        UUID userId = UUID.randomUUID();

        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .username(userIdentifier)
                .userPassword("root")
                .userRoles(List.of(UserRole.builder()
                        .role(Role.builder()
                                .roleId(UUID.randomUUID())
                                .roleCode(RoleEnum.ADMIN)
                                .roleName(RoleEnum.ADMIN.getRoleName())
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
