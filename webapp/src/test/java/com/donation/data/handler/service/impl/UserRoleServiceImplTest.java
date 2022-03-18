package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.exception.InvalidMessageException;
import com.donation.common.management.entity.Role;
import com.donation.common.management.entity.UserAccount;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.entity.UserRole;
import com.donation.common.management.mapper.RoleMapper;
import com.donation.common.management.mapper.UserAccountMapper;
import com.donation.common.management.mapper.UserRoleMapper;
import com.donation.common.management.repository.RoleRepository;
import com.donation.common.management.repository.UserAccountRepository;
import com.donation.common.management.repository.UserRoleRepository;
import com.donation.common.security.CustomUserDetail;
import com.donation.common.security.service.RoleService;
import com.donation.common.security.service.impl.RoleServiceImpl;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.data.handler.service.UserRoleService;
import com.donation.data.handler.service.impl.UserAccountDetailsServiceImpl;
import com.donation.data.handler.service.impl.UserRoleServiceImpl;
import com.donation.management.dto.RoleDto;
import com.donation.management.dto.UserAccountDto;
import com.donation.management.dto.UserRoleDto;
import com.donation.management.model.ProvideRoleRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserRoleServiceImplTest {

    @InjectMocks
    private UserRoleService userRoleServiceTest = new UserRoleServiceImpl();

    @Mock
    private RoleService roleService = new RoleServiceImpl();

    @Mock
    private UserAccountDetailsService userAccountDetailsService = new UserAccountDetailsServiceImpl();

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Spy
    private RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);

    @Spy
    private UserRoleMapper userRoleMapper = Mappers.getMapper(UserRoleMapper.class);

    @Spy
    private UserAccountMapper userAccountMapper = Mappers.getMapper(UserAccountMapper.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserRolesWithAdminRoleAndClientRole() {
        // Create current roles of user
        List<RoleEnum> roleEnums = new ArrayList<>();
        roleEnums.add(RoleEnum.ADMIN);
        roleEnums.add(RoleEnum.CLIENT);

        List<RoleDto> roles = createRoleDtos(createRoles(List.of(RoleEnum.values())));

        // when role dto
        when(roleService.getRoles()).thenReturn(roles);

        // when user role
        UUID userId = UUID.randomUUID();
        List<UserRole> userRoles = createUserRoles(userId, roleEnums);
        when(userRoleRepository.findByUserId(userId))
                .thenReturn(userRoles);

        // Call the function to test
        List<UserRoleDto> userRoleDtos =
                userRoleServiceTest.getAllRolesForUserWithCheck(userId);

        List<RoleDto> roleDtos = userRoleDtos.stream()
                .map(UserRoleDto::getRole)
                .collect(Collectors.toList());

        assertThat(roleDtos)
                .hasSameSizeAs(createRoles(List.of(RoleEnum.values())))
                .containsOnlyOnceElementsOf(roleDtos.stream()
                        .filter(r -> r.getRoleCode().equals(RoleEnum.ADMIN) && r.getCheck())
                        .collect(Collectors.toList()))
                .containsOnlyOnceElementsOf(roleDtos.stream()
                        .filter(r -> r.getRoleCode().equals(RoleEnum.CLIENT) && r.getCheck())
                        .collect(Collectors.toList()));
    }

    @Test
    public void testGetUserRolesWithAdminRole() {
        // Create current roles of user
        List<RoleEnum> roleEnums = new ArrayList<>();
        roleEnums.add(RoleEnum.ADMIN);

        List<RoleDto> roles = createRoleDtos(createRoles(List.of(RoleEnum.values())));

        // when role dto
        when(roleService.getRoles()).thenReturn(roles);

        // when user role
        UUID userId = UUID.randomUUID();
        List<UserRole> userRoles = createUserRoles(userId, roleEnums);
        when(userRoleRepository.findByUserId(userId))
                .thenReturn(userRoles);

        // Call the function to test
        List<UserRoleDto> userRoleDtos =
                userRoleServiceTest.getAllRolesForUserWithCheck(userId);

        List<RoleDto> adminRole = userRoleDtos.stream()
                .map(UserRoleDto::getRole)
                .collect(Collectors.toList());

        assertThat(adminRole)
                .hasSameSizeAs(createRoles(List.of(RoleEnum.values())))
                .containsOnlyOnceElementsOf(adminRole.stream()
                        .filter(r -> r.getRoleCode().equals(RoleEnum.ADMIN) && r.getCheck())
                        .collect(Collectors.toList()));
    }

    @Test
    public void testUpdateRoleForExistedRole() throws InvalidMessageException {
        buildForAdmin();

        UUID userId = UUID.randomUUID();
        // Build user roles for client
        List<UserRole> userRolesClient = createUserRoles(userId, List.of(RoleEnum.values()));

        List<UserRoleDto> userRoleDtos = createUserRoleDtos(userRolesClient);
        // Check for test
        userRoleDtos.forEach((ur) -> {
            if (ur.getRole().getRoleCode().equals(RoleEnum.CLIENT)) {
                ur.getRole().setCheck(Boolean.TRUE);
            } else if (ur.getRole().getRoleCode().equals(RoleEnum.ADMIN)) {
                ur.getRole().setCheck(Boolean.FALSE);
            }
        });

        // UserAccountDto for input
        UserAccountDto userAccountDto = UserAccountDto.builder()
                .userId(userId)
                .username("user")
                .userRoles(userRoleDtos)
                .build();

        // UserAccount for output
        Optional<UserAccount> userAccountClient = Optional.ofNullable(UserAccount.builder()
                .userId(userId)
                .userRoles(userRolesClient)
                .build());
        when(userAccountRepository.findById(userAccountDto.getUserId())).thenReturn(userAccountClient);

        // when handle
        Optional<Role> roleAdmin = Optional.ofNullable(Role.builder()
                .roleId(userRoleDtos.get(0).getRole().getRoleId())
                .roleName(RoleEnum.ADMIN.getRoleName())
                .roleCode(RoleEnum.ADMIN)
                .build());
        Optional<Role> roleClient = Optional.ofNullable(Role.builder()
                .roleId(userRoleDtos.get(1).getRole().getRoleId())
                .roleName(RoleEnum.CLIENT.getRoleName())
                .roleCode(RoleEnum.CLIENT)
                .build());
        when(roleRepository.findById(userRoleDtos.get(0).getRole().getRoleId())).thenReturn(roleAdmin);

        when(roleRepository.findById(userRoleDtos.get(1).getRole().getRoleId())).thenReturn(roleClient);

        // when save
        UserAccount savedUser = userAccountClient.get();
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUser);
        when(userAccountMapper.toDto(savedUser)).thenCallRealMethod();

        // Call the function to test
        userRoleServiceTest.updateRole(userAccountDto);

        ArgumentCaptor<UserAccount> userAccountArgumentCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository)
                .save(userAccountArgumentCaptor.capture());

        UserAccount capturedUserAccount = userAccountArgumentCaptor.getValue();

        assertThat(capturedUserAccount.getUserRoles())
                .hasSize(1)
                .hasSameElementsAs(capturedUserAccount.getUserRoles()
                        .stream()
                        .filter(ur -> ur.getRole().getRoleCode().equals(RoleEnum.CLIENT))
                        .collect(Collectors.toList()));
    }

    @Test
    public void testUpdateRoleForNewRole() throws InvalidMessageException {
        buildForAdmin();

        UUID userId = UUID.randomUUID();
        // Build user roles for client
        List<UserRole> userRolesClient = createUserRoles(userId, List.of(RoleEnum.CLIENT));

        List<UserRoleDto> userRoleDtos = createUserRoleDtos(userRolesClient);
        // Check for test
        userRoleDtos.forEach((ur) -> {
            if (ur.getRole().getRoleCode().equals(RoleEnum.CLIENT)) {
                ur.getRole().setCheck(Boolean.TRUE);
            }
        });

        // UserAccountDto for input
        UserAccountDto userAccountDto = UserAccountDto.builder()
                .userId(userId)
                .username("user")
                .userRoles(userRoleDtos)
                .build();

        // UserAccount for output
        Optional<UserAccount> userAccountClient = Optional.ofNullable(UserAccount.builder()
                .userId(userId)
                .userRoles(new ArrayList<>())
                .build());
        when(userAccountRepository.findById(userAccountDto.getUserId())).thenReturn(userAccountClient);

        // when handle
        Optional<Role> role = Optional.ofNullable(Role.builder()
                .roleId(userRoleDtos.get(0).getRole().getRoleId())
                .roleName(RoleEnum.CLIENT.getRoleName())
                .roleCode(RoleEnum.CLIENT)
                .build());
        when(roleRepository.findById(userRoleDtos.get(0).getRole().getRoleId())).thenReturn(role);

        // when save
        UserAccount savedUser = userAccountClient.get();
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUser);
        when(userAccountMapper.toDto(savedUser)).thenCallRealMethod();

        // Call the function to test
        userRoleServiceTest.updateRole(userAccountDto);

        ArgumentCaptor<UserAccount> userAccountArgumentCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository)
                .save(userAccountArgumentCaptor.capture());

        UserAccount capturedUserAccount = userAccountArgumentCaptor.getValue();

        assertThat(capturedUserAccount.getUserRoles())
                .hasSameElementsAs(capturedUserAccount.getUserRoles().stream()
                        .filter(ur -> ur.getRole().getRoleCode().equals(RoleEnum.CLIENT))
                        .collect(Collectors.toList()));
    }

    @Test
    public void testUpdateRoleForDeactivateRole() throws InvalidMessageException {
        buildForAdmin();

        UUID userId = UUID.randomUUID();
        // Build user roles for client
        List<RoleEnum> roleClientEnums = List.of(RoleEnum.CLIENT);

        List<UserRole> userRolesClient = createUserRoles(userId, roleClientEnums);

        List<UserRoleDto> userRoleDtos = createUserRoleDtos(userRolesClient);
        // Check for test
        userRoleDtos.forEach((ur) -> {
            if (ur.getRole().getRoleCode().equals(RoleEnum.CLIENT)) {
                ur.getRole().setCheck(Boolean.FALSE);
            }
        });

        // UserAccountDto for input
        UserAccountDto userAccountDto = UserAccountDto.builder()
                .userId(userId)
                .username("user")
                .userRoles(userRoleDtos)
                .build();

        // UserAccount for output
        Optional<UserAccount> userAccountClient = Optional.ofNullable(UserAccount.builder()
                .userId(userId)
                .userRoles(userRolesClient)
                .build());
        when(userAccountRepository.findById(userAccountDto.getUserId())).thenReturn(userAccountClient);

        // when handle
        Optional<Role> role = Optional.ofNullable(Role.builder()
                .roleId(userRoleDtos.get(0).getRole().getRoleId())
                .roleName(roleClientEnums.get(0).getRoleName())
                .roleCode(roleClientEnums.get(0))
                .build());
//        when(roleRepository.findById(userRoleDtos.get(0).getRole().getRoleId())).thenReturn(role);

        // when save
        UserAccount savedUser = userAccountClient.get();
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUser);
        when(userAccountMapper.toDto(savedUser)).thenCallRealMethod();

        // Call the function to test
        userRoleServiceTest.updateRole(userAccountDto);

        ArgumentCaptor<UserAccount> userAccountArgumentCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository)
                .save(userAccountArgumentCaptor.capture());

        UserAccount capturedUserAccount = userAccountArgumentCaptor.getValue();

//        assertThat(capturedUserAccount).isEqualTo(savedUser);

        assertThat(capturedUserAccount.getUserRoles())
                .hasSize(0);
        assertThat(capturedUserAccount.getIsActive()).isFalse();
    }

    @Test
    public void testUpdatePermissionActive() throws InvalidMessageException {
        buildForAdmin();

        // Build request
        UUID userOne = UUID.randomUUID();
        UUID userTwo = UUID.randomUUID();
        List<UUID> users = List.of(userOne, userTwo);
        ProvideRoleRequest roleRequest = ProvideRoleRequest.builder()
                .users(users)
                .active(Boolean.TRUE)
                .build();

        // Build user
        UserAccount userAccountOne = UserAccount.builder()
                .userId(userOne)
                .userRoles(new ArrayList<>())
                .build();
        UserAccount userAccountTwo = UserAccount.builder()
                .userId(userTwo)
                .userRoles(new ArrayList<>())
                .build();
        List<UserAccount> userAccounts = List.of(userAccountOne, userAccountTwo);

        when(userAccountRepository.findAllById(roleRequest.getUsers())).thenReturn(userAccounts);

        when(roleRepository.findByRoleCode(RoleEnum.CLIENT))
                .thenReturn(createRoles(List.of(RoleEnum.CLIENT)).get(0));

        // Call the function to test
        userRoleServiceTest.updatePermission(roleRequest);

        ArgumentCaptor<List<UserAccount>> listOfUserAccountCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(userAccountRepository)
                .saveAll(listOfUserAccountCaptor.capture());

        List<UserAccount> capturedUserAccount = listOfUserAccountCaptor.getValue();

        assertThat(capturedUserAccount.get(0).getUserRoles()).isNotEmpty();
        assertThat(capturedUserAccount.get(1).getUserRoles()).isNotEmpty();
    }

    @Test
    public void testUpdatePermissionDeactivate() throws InvalidMessageException {
        buildForAdmin();

        // Build request
        UUID userOne = UUID.randomUUID();
        UUID userTwo = UUID.randomUUID();
        List<UUID> users = List.of(userOne, userTwo);
        ProvideRoleRequest roleRequest = ProvideRoleRequest.builder()
                .users(users)
                .active(Boolean.FALSE)
                .build();

        // Build user
        UserAccount userAccountOne = UserAccount.builder()
                .userId(userOne)
                .userRoles(new ArrayList<>())
                .build();
        UserAccount userAccountTwo = UserAccount.builder()
                .userId(userTwo)
                .userRoles(new ArrayList<>())
                .build();
        List<UserAccount> userAccounts = List.of(userAccountOne, userAccountTwo);

        when(userAccountRepository.findAllById(roleRequest.getUsers())).thenReturn(userAccounts);


        // Call the function to test
        userRoleServiceTest.updatePermission(roleRequest);

        ArgumentCaptor<List<UserAccount>> listOfUserAccountCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(userAccountRepository)
                .saveAll(listOfUserAccountCaptor.capture());

        List<UserAccount> capturedUserAccount = listOfUserAccountCaptor.getValue();

        assertThat(capturedUserAccount.get(0).getUserRoles()).isEmpty();
        assertThat(capturedUserAccount.get(1).getUserRoles()).isEmpty();
    }

    public List<UserRole> createUserRoles(UUID userId, List<RoleEnum> roleEnums) {
        List<Role> roles = createRoles(List.of(RoleEnum.values()));

        List<UserRole> userRoles = new ArrayList<>();

        for (RoleEnum roleEnum : roleEnums) {
            UserAccount userAccount = UserAccount.builder()
                    .userId(userId)
                    .build();

            UserRole userRole = UserRole.builder()
                    .id(UUID.randomUUID())
                    .userAccount(userAccount)
                    .role(roles.stream().filter(r -> r.getRoleCode().equals(roleEnum)).collect(Collectors.toList()).stream().findFirst().orElse(null))
                    .build();

            userRoles.add(userRole);
        }
        return userRoles;
    }

    public List<Role> createRoles(List<RoleEnum> roleEnums) {
        List<Role> roles = new ArrayList<>();
        for (RoleEnum roleEnum : roleEnums) {
            UUID roleId = UUID.randomUUID();
            Role role = Role.builder()
                    .roleId(roleId)
                    .roleName(roleEnum.getRoleName())
                    .roleCode(roleEnum)
                    .build();
            roles.add(role);
        }
        return roles;
    }

    public List<RoleDto> createRoleDtos(List<Role> roles) {
        return roles.stream().map(roleMapper::toDto).collect(Collectors.toList());
    }

    public List<UserRoleDto> createUserRoleDtos(List<UserRole> userRoles) {
        return userRoles.stream().map(userRoleMapper::toDto).collect(Collectors.toList());
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
