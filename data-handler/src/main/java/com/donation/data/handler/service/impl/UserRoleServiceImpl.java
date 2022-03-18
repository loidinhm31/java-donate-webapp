package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.Role;
import com.donation.common.management.entity.UserAccount;
import com.donation.common.management.entity.UserRole;
import com.donation.common.management.mapper.UserAccountMapper;
import com.donation.common.management.mapper.UserRoleMapper;
import com.donation.common.management.repository.RoleRepository;
import com.donation.common.management.repository.UserAccountRepository;
import com.donation.common.management.repository.UserRoleRepository;
import com.donation.common.security.service.RoleService;
import com.donation.data.handler.service.UserRoleService;
import com.donation.management.dto.RoleDto;
import com.donation.management.dto.UserAccountDto;
import com.donation.management.dto.UserRoleDto;
import com.donation.management.model.ProvideRoleRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.donation.common.core.constant.MessageConstants.AUTHORIZE_EXCEPTION;

@Slf4j
@Service
public class UserRoleServiceImpl extends BaseInitiatedServiceImpl implements UserRoleService {
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public List<UserRoleDto> getAllRolesForUserWithCheck(UUID userId) {
        List<RoleDto> roleDtos =
                roleService.getRoles();

        List<UserRoleDto> userRoleDtos = getUserRoles(userId);

        List<RoleDto> tempRole = new ArrayList<>();
        for (RoleDto roleDto : roleDtos) {
            for (UserRoleDto userRoleDto : userRoleDtos) {
                if (userRoleDto.getRole().equals(roleDto)) {
                    userRoleDto.getRole().setCheck(Boolean.TRUE);
                    tempRole.add(userRoleDto.getRole());
                }
            }
        }
        roleDtos.removeAll(tempRole);

        if (CollectionUtils.isNotEmpty(roleDtos)) {
            for (RoleDto r : roleDtos) {
                UserRoleDto nonAddedUserRole = new UserRoleDto();
                nonAddedUserRole.setRole(r);
                userRoleDtos.add(0, nonAddedUserRole);
            }
        }

        userRoleDtos = userRoleDtos.stream()
                .distinct()
                .sorted(Comparator.comparing(ur -> ur.getRole().getRoleName()))
                .collect(Collectors.toList());

        return userRoleDtos;
    }

    @Override
    public UserAccountDto updateRole(UserAccountDto userAccountDto) throws InvalidMessageException {
        if (checkPermissionWithRole()) {
            if (Objects.nonNull(userAccountDto.getUserId())) {
                // Check user exists
                Optional<UserAccount> existedUserAccount = userAccountRepository.findById(userAccountDto.getUserId());
                if (existedUserAccount.isEmpty()) {
                    throw new InvalidMessageException("User is not exist");
                }

                UserAccount userAccount = existedUserAccount.get();

                // Get current business roles of the user
                List<RoleDto> businessRoles = userAccountDto.getUserRoles()
                        .stream()
                        .map(UserRoleDto::getRole)
                        .collect(Collectors.toList());

                List<UserRoleDto> checkRoles = userAccountDto.getUserRoles()
                        .stream()
                        .filter(ur -> ur.getRole().getCheck())
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(businessRoles)
                        && CollectionUtils.isNotEmpty(checkRoles)) {
                    // Add user role for user account
                    for (RoleDto role : businessRoles) {
                        UserRole userRoleOfUser = userAccount.getUserRoles().stream()
                                .filter(ur -> ur.getRole().getRoleId().equals(role.getRoleId()))
                                .collect(Collectors.toList()).stream()
                                .findFirst().orElse(new UserRole());

                        updateUserRole(userAccount, role, userAccount.getUserRoles().indexOf(userRoleOfUser));
                    }
                } else if (CollectionUtils.isEmpty(checkRoles)) {
                    // Delete all roles if current role list is empty
                    List<UserRole> useRoles = userAccount.getUserRoles();
                    userRoleRepository.deleteAll(useRoles);

                    // Set deactivate for user account
                    userAccount.getUserRoles().removeAll(useRoles);
                    userAccount.setIsActive(Boolean.FALSE);
                }

                UserAccount savedUser = userAccountRepository.save(userAccount);

                UserAccountDto responseUserAccountDto = userAccountMapper.toDto(savedUser);
                return responseUserAccountDto;
            } else {
                throw new InvalidMessageException("User id must not be empty");
            }
        } else throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
    }

    @Override
    public void updatePermission(ProvideRoleRequest roleRequest) throws InvalidMessageException {
        if (checkPermissionWithRole()) { // Check permission
            if (Objects.nonNull(roleRequest)
                    && CollectionUtils.isNotEmpty(roleRequest.getUsers())
                    && Objects.nonNull(roleRequest.getActive())) {

                List<UserAccount> userAccounts = userAccountRepository.findAllById(roleRequest.getUsers());

                if (CollectionUtils.isNotEmpty(userAccounts)) {
                    if (roleRequest.getActive()) {

                        Role clientRole = roleRepository.findByRoleCode(RoleEnum.CLIENT);

                        for (UserAccount userAccount : userAccounts) {
                            List<UserRole> providedRole = userAccount.getUserRoles().stream()
                                    .filter(ur -> ur.getRole().getRoleCode().equals(RoleEnum.CLIENT))
                                    .collect(Collectors.toList());

                            if (CollectionUtils.isEmpty(providedRole)) {
                                UserRole userRole = new UserRole();
                                userRole.setUserAccount(userAccount);
                                userRole.setRole(clientRole);
                                userAccount.getUserRoles().add(userRole);

                                userAccount.setIsActive(Boolean.TRUE);
                            }
                        }
                    } else { // Deactivate users
                        for (UserAccount userAccount : userAccounts) {
                            // Set deactivate for user account
                            userAccount.setIsActive(Boolean.FALSE);
                        }
                    }
                    userAccountRepository.saveAll(userAccounts);
                }
            }
        } else throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
    }

    @Override
    public void changeActive(String userId, Boolean isActive) throws InvalidMessageException {
        if (checkPermissionWithRole()) {
            // Check user exists
            Optional<UserAccount> existedUserAccount = userAccountRepository.findById(UUID.fromString(userId));
            if (existedUserAccount.isEmpty()) {
                throw new InvalidMessageException("User is not exist");
            }

            UserAccount userAccount = existedUserAccount.get();
            log.info("Change permission for {} to {}", userAccount.getUsername(), isActive);
            if (isActive) {
                if (CollectionUtils.isEmpty(userAccount.getUserRoles())) {
                    Role clientRole = roleRepository.findByRoleCode(RoleEnum.CLIENT);
                    UserRole userRole = new UserRole();
                    userRole.setUserAccount(userAccount);
                    userRole.setRole(clientRole);
                    userAccount.getUserRoles().add(userRole);
                }
                userAccount.setIsActive(Boolean.TRUE);
            } else {
                userAccount.setIsActive(Boolean.FALSE);
            }
            userAccountRepository.save(userAccount);
        } else throw new InvalidMessageException(AUTHORIZE_EXCEPTION);
    }

    private List<UserRoleDto> getUserRoles(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .map(userRoleMapper::toDto)
                .collect(Collectors.toList());
    }

    private Role handleUserRoleForUser(RoleDto role) throws InvalidMessageException {
        // Handle to get role
        Optional<Role> roleOptional = roleRepository.findById(role.getRoleId());
        if (roleOptional.isEmpty()) {
            throw new InvalidMessageException("Role id invalid");
        }
        return roleOptional.get();
    }

    private void updateUserRole(UserAccount userAccount, RoleDto role, int index) throws InvalidMessageException {
        Role roleOnSystem = handleUserRoleForUser(role);
        if (index >= 0) {
            if (CollectionUtils.isNotEmpty(userAccount.getUserRoles())) {
                if (role.getCheck()) {
                    // Change current role for the existed user
                    userAccount.getUserRoles().get(index).setRole(roleOnSystem);
                } else {
                    userRoleRepository.delete(userAccount.getUserRoles().get(index));
                    userAccount.getUserRoles().remove(index);
                }
            }
        } else {
            if (role.getCheck()) {
                // Set permission (role) for a new user
                UserRole userRole = new UserRole();
                userRole.setUserAccount(userAccount);
                userRole.setRole(roleOnSystem);
                userAccount.getUserRoles().add(userRole);

                // Set active for user account
                userAccount.setIsActive(Boolean.TRUE);
            }
        }
    }

}
