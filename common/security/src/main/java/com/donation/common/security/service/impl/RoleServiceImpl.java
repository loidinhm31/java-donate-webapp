package com.donation.common.security.service.impl;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.management.entity.Role;
import com.donation.common.management.mapper.RoleMapper;
import com.donation.common.management.repository.RoleRepository;
import com.donation.common.security.service.RoleService;
import com.donation.management.dto.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<RoleDto> getRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDto> roleDtos = roles.stream()
                .map(role -> roleMapper.toDto(role))
                .collect(Collectors.toList());
        return roleDtos.stream().sorted(Comparator.comparing(RoleDto::getRoleName))
                .collect(Collectors.toList());

    }

    @Override
    public Role findRole(RoleEnum roleEnum) {
        return roleRepository.findByRoleCode(roleEnum);
    }
}
