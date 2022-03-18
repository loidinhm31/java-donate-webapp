package com.donation.webapp.controllers.management;

import com.donation.common.core.enums.BeneficiaryType;
import com.donation.common.core.enums.ProjectSearchColumn;
import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.enums.SortType;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.repository.projection.ProjectProjection;
import com.donation.data.handler.service.BeneficiaryService;
import com.donation.data.handler.service.ProjectService;
import com.donation.management.dto.BeneficiaryDto;
import com.donation.management.dto.ProjectDto;
import com.donation.management.dto.ProjectRequestDto;
import com.donation.management.model.ProjectSearchFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;
import static com.donation.common.core.constant.UrlMapping.ADMIN_PROJECT;

@Controller
@RequestMapping(ADMIN_PROJECT)
public class ProjectManagementController {

    @Value("${cloudinary.path}")
    private String rootPath;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @GetMapping
    public String showProjectManagement(Model theModel) {
        ProjectSearchFilter projectSearchFilter = ProjectSearchFilter.builder()
                .startPage(0)
                .pageSize(DEFAULT_PAGE_SIZE)
                .sortBy(ProjectSearchColumn.UPDATED_AT)
                .sortType(SortType.DESC)
                .build();

        List<ProjectDto> projectCodes = projectService.getProjectCodes();

        ProjectStatusEnum[] projectStatuses = ProjectStatusEnum.values();

        Page<ProjectProjection> projects =
                projectService.getProjectsForAdmin(projectSearchFilter);

        theModel.addAttribute("content", projects);
        theModel.addAttribute("projectCodes", projectCodes);
        theModel.addAttribute("projectStatuses", projectStatuses);

        return "views/admin/project/admin_project";
    }

    @PostMapping("/search")
    public String searchUsers(@RequestBody ProjectSearchFilter projectSearchFilter,
                              Model theModel) {
        projectSearchFilter.setStartPage(projectSearchFilter.getStartPage() - 1);
        projectSearchFilter.setSortBy(ProjectSearchColumn.UPDATED_AT);
        projectSearchFilter.setSortType(SortType.DESC);

        Page<ProjectProjection> projects =
                projectService.getProjectsForAdmin(projectSearchFilter);
        theModel.addAttribute("content", projects);
        return "views/admin/project/admin_project_content :: project_table";
    }

    @RequestMapping(value = {"/create", "/edit/{projectId}"}, method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public String showCreateProject(@ModelAttribute ProjectRequestDto projectRequestDto,
                                    @PathVariable(required = false) Optional<String> projectId, Model theModel) {

        List<BeneficiaryDto> beneficiaryDtos = beneficiaryService.getBeneficiaries();
        theModel.addAttribute("beneficiaryDtos", beneficiaryDtos);
        if (projectId.isPresent()) {
            try {
                ProjectDto projectDto = projectService.getProject(projectId.get());

                projectRequestDto.setProjectId(projectDto.getProjectId());
                projectRequestDto.setStatus(projectDto.getStatus());
                projectRequestDto.setProjectName(projectDto.getProjectName());
                projectRequestDto.setProjectSummary(projectDto.getProjectSummary());
                projectRequestDto.setProjectContent(projectDto.getProjectContent());
                projectRequestDto.setStartTime(projectDto.getStartTime());
                projectRequestDto.setTargetTime(projectDto.getTargetTime());
                projectRequestDto.setTargetMoney(projectDto.getTargetMoney());
                projectRequestDto.setBeneficiaryId(projectDto.getBeneficiary().getBeneficiaryId());
                projectRequestDto.setFilePath(Objects.nonNull(projectDto.getFilePath()) ? projectDto.getFilePath() : null);

                theModel.addAttribute("rootPath", rootPath);
                theModel.addAttribute("projectRequestDto", projectRequestDto);
                return "views/admin/project/project_form";
            } catch (InvalidMessageException e) {
                theModel.addAttribute("message", e.getMessage());
                return "views/message";
            }
        }

        theModel.addAttribute("projectRequestDto", new ProjectRequestDto());
        return "views/admin/project/project_form";
    }

    @PostMapping(value = "/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String saveProject(@ModelAttribute ProjectRequestDto projectRequestDto,
                              @RequestParam("projectFile") MultipartFile multipartFile,
                              RedirectAttributes redirectAttributes) {

        try {
            projectService.saveOrUpdate(projectRequestDto, multipartFile);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/project/create";
        }
        return "redirect:/admin/project";
    }

    @GetMapping("/beneficiary/create")
    public String showCreateBeneficiary(Model theModel) {

        BeneficiaryType[] beneficiaryTypes = BeneficiaryType.values();

        theModel.addAttribute("beneficiaryDto", new BeneficiaryDto());
        theModel.addAttribute("beneficiaryTypes", beneficiaryTypes);

        return "views/admin/project/beneficiary_form";
    }

    @PostMapping("/beneficiary/create")
    public String saveBeneficiary(BeneficiaryDto beneficiaryDto, RedirectAttributes redirectAttributes) {

        try {
            beneficiaryService.createBeneficiary(beneficiaryDto);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/project/beneficiary";
        }
        return "redirect:/admin/project/create";
    }

    @GetMapping("/time-extend")
    public String fetchProjectTime(@RequestParam String projectId, Model theModel) {
        ProjectDto projectDto = projectService.getProjectForTimeExtend(projectId);

        theModel.addAttribute("project", projectDto);
        return "views/admin/project/project_modal :: time_detail";
    }

    @GetMapping("/delete")
    public String fetchProjectId(@RequestParam String projectId, Model theModel) {

        theModel.addAttribute("projectId", projectId);
        return "views/admin/project/project_modal :: project_detele";
    }
}
