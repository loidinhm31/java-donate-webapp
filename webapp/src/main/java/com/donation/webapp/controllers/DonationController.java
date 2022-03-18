package com.donation.webapp.controllers;

import com.donation.data.handler.service.DonationTimeService;
import com.donation.management.dto.DonationTimeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;

@Controller
@RequestMapping("/donation")
public class DonationController {
    @Autowired
    private DonationTimeService donationTimeService;

    @GetMapping("/donators/{projectId}")
    public String showDonatorList(@PathVariable String projectId,
                                  @RequestParam Optional<Integer> page,
                                  @RequestParam Optional<Integer> size,
                                  Model theModel) {
        // Pagination
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.Direction.ASC, "created_at");

        Page<DonationTimeDto> donators =
                donationTimeService.getDonators(projectId, pageable);
        theModel.addAttribute("content", donators);

        return "views/project/project_tab_fragments :: donator";
    }
}
