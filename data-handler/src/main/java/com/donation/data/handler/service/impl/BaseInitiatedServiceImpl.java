package com.donation.data.handler.service.impl;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.management.entity.Project;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.entity.UserRole;
import com.donation.common.security.SecurityUtils;
import com.donation.data.handler.service.ProjectService;
import com.donation.data.handler.service.UserAccountDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;

import static com.donation.common.core.enums.RoleEnum.ADMIN;

@Slf4j
@Service
@Transactional(rollbackOn = Exception.class)
public class BaseInitiatedServiceImpl {

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private ProjectService projectService;

    protected Boolean checkPermissionWithRole() {
        log.info("Checking permission for admin...");
        String identifier = SecurityUtils.getUserIdentifier();
        UserAccountDetail userAccountDetail = userAccountDetailsService.findUserByIdentifier(identifier);
        if (Objects.nonNull(userAccountDetail)) {
            for (UserRole userRole : userAccountDetail.getUserAccount().getUserRoles()) {
                if (userRole.getRole().getRoleCode().equals(ADMIN)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    protected Boolean checkValidProjectStatus(ProjectStatusEnum projectStatus) {
        log.info("Checking valid status for the project...");
        if (projectStatus.equals(ProjectStatusEnum.IN_PROGRESS)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    protected void updateTotalMoneyForProject(Project project, Float amount) {
        log.info("Updating total money for {} with amount {}", project.getProjectId(), amount);
        project.setCurrentMoney(project.getCurrentMoney() + amount);

        // Update status to ENDED if project reach goal
        if (project.getCurrentMoney() >= project.getTargetMoney()) {
            project.setStatus(ProjectStatusEnum.ENDED);
        }
        projectService.updateProject(project);
    }
}
