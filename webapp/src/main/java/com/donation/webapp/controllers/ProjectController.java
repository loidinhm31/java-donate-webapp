package com.donation.webapp.controllers;

import com.donation.common.core.enums.ProjectSearchColumn;
import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.enums.SortType;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.entity.UserRole;
import com.donation.common.security.CustomUserDetail;
import com.donation.common.security.SecurityUtils;
import com.donation.common.security.service.UserService;
import com.donation.data.handler.service.FollowerProjectService;
import com.donation.data.handler.service.ProjectService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.management.dto.ProjectDto;
import com.donation.management.model.ProjectSearchFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;

@Controller
@RequestMapping("/project")
public class ProjectController {

    @Value("${cloudinary.path}")
    private String rootPath;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FollowerProjectService followerProjectService;

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @GetMapping
    public String showProjects(Model theModel) {
        ProjectSearchFilter projectSearchFilter = ProjectSearchFilter.builder()
                .startPage(0)
                .pageSize(DEFAULT_PAGE_SIZE)
                .sortBy(ProjectSearchColumn.UPDATED_AT)
                .sortType(SortType.DESC)
                .build();

        ProjectStatusEnum[] projectStatusEnums = ProjectStatusEnum.values();

        Page<ProjectDto> projects =
                projectService.getProjectsForUser(projectSearchFilter);

        theModel.addAttribute("rootPath", rootPath);
        theModel.addAttribute("statuses", projectStatusEnums);
        theModel.addAttribute("content", projects);
        theModel.addAttribute("currentDate", new Date());
        return "views/project/project";
    }

    @PostMapping
    public String filterProjects(@RequestBody ProjectSearchFilter projectSearchFilter, Model theModel) {

        projectSearchFilter.setStartPage(projectSearchFilter.getStartPage() - 1);
        projectSearchFilter.setSortBy(ProjectSearchColumn.UPDATED_AT);
        projectSearchFilter.setSortType(SortType.DESC);

        ProjectStatusEnum[] projectStatusEnums = ProjectStatusEnum.values();

        Page<ProjectDto> projects =
                projectService.getProjectsForUser(projectSearchFilter);

        theModel.addAttribute("rootPath", rootPath);
        theModel.addAttribute("statuses", projectStatusEnums);
        theModel.addAttribute("content", projects);
        theModel.addAttribute("currentDate", new Date());
        return "views/project/project :: project_content";
    }

    @GetMapping("/{projectId}")
    public String showProjects(@PathVariable String projectId, Model theModel) throws InvalidMessageException {

        ProjectDto projectDto =
                projectService.getProject(projectId);

        if (projectDto.getStatus().equals(ProjectStatusEnum.CREATED)) {
            try {
                String userIdentifier = SecurityUtils.getUserIdentifier();
                UserAccountDetail userAccountDetail =
                        userAccountDetailsService.findUserByIdentifier(userIdentifier);

                if (Objects.nonNull(userAccountDetail)) {   // Check user
                    UserRole adminRole = userAccountDetail.getUserAccount().getUserRoles().stream()
                            .filter(ur -> ur.getRole().getRoleCode().equals(RoleEnum.ADMIN))
                            .findFirst().orElse(null);
                    if (Objects.nonNull(adminRole)) {   // Check admin role
                        theModel.addAttribute("rootPath", rootPath);
                        theModel.addAttribute("projectId", projectId);
                        theModel.addAttribute("detail", projectDto);
                        theModel.addAttribute("currentDate", new Date());
                        return "views/project/project_detail";
                    }
                }
            } catch (Exception e) {
                System.out.println("ANONYMOUS...");
            }
            theModel.addAttribute("message", "This project was not available currently. Please comeback later." +
                    " Thank for your support.");
            return "views/message";
        }

        theModel.addAttribute("rootPath", rootPath);
        theModel.addAttribute("projectId", projectId);
        theModel.addAttribute("detail", projectDto);
        theModel.addAttribute("currentDate", new Date());
        theModel.addAttribute("isFollow", checkFollower(projectId));
        return "views/project/project_detail";
    }

    @GetMapping("/list")
    public String loadList(Model theModel) {

        List<ProjectDto> projectDtoList = projectService.getProjectsLoader();

        theModel.addAttribute("projects", projectDtoList);
        return "views/project/project_modal :: donate_content_free";
    }

    private Boolean checkFollower(String projectId) {
        String identifier;
        try {
            identifier = SecurityUtils.getUserIdentifier();
        } catch (Exception exception) {
            return Boolean.FALSE;
        }
        if (StringUtils.isNotEmpty(identifier)) {
            return followerProjectService.checkFollowByProject(projectId, identifier);
        }
        return Boolean.FALSE;
    }
}
