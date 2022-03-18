package com.donation.webapp.controllers.rest;

import com.donation.common.management.mapper.RoleMapper;
import com.donation.common.management.mapper.UserAccountMapper;
import com.donation.common.management.mapper.UserRoleMapper;
import com.donation.common.management.repository.RoleRepository;
import com.donation.common.management.repository.UserAccountDetailRepository;
import com.donation.common.management.repository.UserAccountRepository;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserRestController {

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    UserAccountDetailRepository userAccountDetailRepository;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RoleMapper roleMapper;

    @Autowired
    UserAccountMapper userAccountMapper;

    @Autowired
    UserRoleMapper  userRoleMapper;

    @Autowired
    UserRoleService userRoleService;


    @PostMapping("/username/isUnique")
    public String checkDuplicateUsername(String username) {
        return userAccountDetailsService.isUsernameUnique(username) ? "OK" : "Duplicated";
    }

    @PostMapping("/email/isUnique")
    public String checkDuplicateEmail(String email) {
        return userAccountDetailsService.isEmailUnique(email) ? "OK" : "Duplicated";
    }

}
