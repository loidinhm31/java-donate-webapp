package com.donation.webapp.controllers.management;

import com.donation.data.handler.service.ProjectService;
import com.donation.data.handler.service.impl.DashboardServiceImpl;
import com.donation.management.dto.ProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/overview")
public class ChartController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public String showChart(Model theModel) {

        List<ProjectDto> projectCodes = projectService.getProjectCodes();

        theModel.addAttribute("projectCodes", projectCodes);
        return "views/admin/chart";
    }
}
