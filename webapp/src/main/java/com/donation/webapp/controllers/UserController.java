package com.donation.webapp.controllers;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.security.CustomUserDetail;
import com.donation.common.security.SecurityUtils;
import com.donation.common.security.service.UserService;
import com.donation.data.handler.service.DonationTimeService;
import com.donation.data.handler.service.FollowerProjectService;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.management.dto.DonationTimeDto;
import com.donation.management.dto.ProjectDto;
import com.donation.management.dto.UserAccountDetailDto;
import com.donation.management.model.ProjectSearchFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;


@Controller
@RequestMapping("/user-detail")
public class UserController {

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private DonationTimeService donationTimeService;

    @Autowired
    private FollowerProjectService followerProjectService;

    @GetMapping
    public String viewDetails(Model theModel) {

        try {
            String userIdentifier = SecurityUtils.getUserIdentifier();
            UserAccountDetailDto userAccountDetailDto =
                    userAccountDetailsService.getUserByIdentifier(userIdentifier);
            theModel.addAttribute("user", userAccountDetailDto);
        } catch (Exception e) {
            theModel.addAttribute("user", new UserAccountDetailDto());
            theModel.addAttribute("message", e.getMessage());
            return "views/message";
        }
        return "views/user/user_detail";
    }

    @PostMapping("/update")
    public String saveDetails(UserAccountDetailDto userAccountDetailDto,
                              RedirectAttributes redirectAttributes) {
        try {
            userAccountDetailDto = userAccountDetailsService
                    .updateUserAccount(userAccountDetailDto);
        } catch (InvalidMessageException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());

        }

        redirectAttributes.addFlashAttribute("message", "Your account details have been updated.");

        return "redirect:/user-detail";
    }

    @GetMapping("/follower")
    public String viewFollow( @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              Model theModel) {

        // Pagination
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        ProjectSearchFilter projectSearchFilter = ProjectSearchFilter.builder()
                .startPage(currentPage - 1)
                .pageSize(pageSize)
                .build();

        Page<ProjectDto> projectsByFollower =
                followerProjectService.getProjectByFollower(projectSearchFilter);
        theModel.addAttribute("content", projectsByFollower);

        if (page.isPresent()) {
            return "views/user/user_follow :: follow_table";
        }
        return "views/user/user_follow";
    }

    @GetMapping("/donation")
    public String viewDonate( @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              Model theModel) {

        // Pagination
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        Page<DonationTimeDto> donatedProjects =
                donationTimeService.getDonationTimeByUser(currentPage - 1, pageSize);
        theModel.addAttribute("content", donatedProjects);

        if (page.isPresent()) {
            return "views/user/user_donation :: donated_table";
        }
        return "views/user/user_donation";
    }
}