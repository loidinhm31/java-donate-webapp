package com.donation.webapp.controllers.management;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.UserRoleService;
import com.donation.management.dto.ProjectDto;
import com.donation.management.dto.UserAccountDetailDto;
import com.donation.management.dto.UserRoleDto;
import com.donation.management.model.FilterLoginOption;
import com.donation.management.model.UserFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;
import static com.donation.common.core.constant.Constants.USER_NOT_LOGIN;
import static com.donation.common.core.constant.UrlMapping.ADMIN_USER;

@Controller
@RequestMapping(ADMIN_USER)
public class UserManagementController {

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public String showUserManagement(Model theModel,
                                     @RequestParam("page") Optional<Integer> page,
                                     @RequestParam("size") Optional<Integer> size) {
        // Pagination
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        UserFilter userFilter = UserFilter.builder()
                .startPage(currentPage - 1)
                .pageSize(pageSize)
                .build();

        Page<UserAccountDetailDto> userAccountDetailDtos =
                userAccountDetailsService.getUsers(userFilter);
        theModel.addAttribute("content", userAccountDetailDtos);

        List<FilterLoginOption> filters = new ArrayList<>();
        filters.add(FilterLoginOption.builder().id("filterLogin1").name("Not Login").value(USER_NOT_LOGIN).build());
        filters.add(FilterLoginOption.builder().id("filterLogin2").name("1 week").value("7").build());
        filters.add(FilterLoginOption.builder().id("filterLogin3").name("1 month").value("30").build());
        filters.add(FilterLoginOption.builder().id("filterLogin4").name("1 quarter").value("90").build());
        filters.add(FilterLoginOption.builder().id("filterLogin5").name("1 year").value("365").build());
        theModel.addAttribute("filterLoginOptionList", filters);
        return "views/admin/user/admin";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              @RequestParam(required = false) String searchKey,
                              @RequestParam(required = false) String option,
                              Model theModel) {

        theModel.addAttribute("searchKey", searchKey);
        theModel.addAttribute("option", option);

        // Pagination
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        UserFilter userFilter = UserFilter.builder()
                .searchKey(searchKey)
                .option(option)
                .startPage(currentPage - 1)
                .pageSize(pageSize)
                .build();

        Page<UserAccountDetailDto> userAccountDetailDtos =
                userAccountDetailsService.getUsers(userFilter);
        theModel.addAttribute("content", userAccountDetailDtos);
        return "views/admin/user/admin_user :: user_table";
    }

    @GetMapping("/role/search")
    public String searchUserRoles(@RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              @RequestParam(required = false) String searchKey,
                              Model theModel) {

        theModel.addAttribute("searchKey", searchKey);

        // Pagination
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        UserFilter userFilter = UserFilter.builder()
                .isVerify(Boolean.TRUE)     // not display users who was not verify account
                .searchKey(searchKey)
                .startPage(currentPage - 1)
                .pageSize(pageSize)
                .build();

        Page<UserAccountDetailDto> userAccountDetailDtos =
                userAccountDetailsService.getUsers(userFilter);
        theModel.addAttribute("content", userAccountDetailDtos);

        return "views/admin/user/admin_role :: user_role_table";
    }

    @GetMapping("/roles/select")
    public String getRoles(Model theModel, @RequestParam(required = false) Optional<UUID> userId) {
        List<UserRoleDto> userRoleDtos;
        if (userId.isPresent()) {
            try {
                UserAccountDetailDto userDetailDto = userAccountDetailsService.getUser(userId.get());
                theModel.addAttribute("user", userDetailDto);
            } catch (InvalidMessageException e) {
                return "redirect:/access-denied";
            }

            userRoleDtos = userRoleService.getAllRolesForUserWithCheck(userId.get());
            theModel.addAttribute("roles", userRoleDtos);
            return "views/admin/user/admin_modal :: admin_roles";
        } else {
            RoleEnum[] roleEnums = RoleEnum.values();

            theModel.addAttribute("roles", roleEnums);
            return "views/admin/user/admin_modal :: admin_roles_multiple";
        }

    }

}
