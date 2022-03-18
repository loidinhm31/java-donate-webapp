package com.donation.data.handler.cronjob;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.management.entity.Project;
import com.donation.common.management.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
@Slf4j
public class UpdateProjectJob {

    @Autowired
    private ProjectRepository projectRepository;

//    @Scheduled(fixedRate = 1000)
    @Scheduled(cron = "0 0 0 ? * *")  // Run every day at 12:00 AM
    public void autoActiveStatusForProject() {
        log.info("Cron job for active status project is running...");
        List<Project> projectList = projectRepository.findAllByStatus(ProjectStatusEnum.CREATED);

        if (CollectionUtils.isNotEmpty(projectList)) {
            Calendar currCal = Calendar.getInstance();
            Calendar projectStartCal = Calendar.getInstance();

            List<Project> changeStatusProjects = new ArrayList<>();
            for (Project project : projectList) {
                // Check date to set IN PROGRESS status
                projectStartCal.setTime(project.getStartTime());
                if ((currCal.get(Calendar.YEAR) == projectStartCal.get(Calendar.YEAR))
                        && (currCal.get(Calendar.MONTH) == projectStartCal.get(Calendar.MONTH))
                        && (currCal.get(Calendar.DAY_OF_MONTH) >= projectStartCal.get(Calendar.DAY_OF_MONTH))
                    || (currCal.get(Calendar.YEAR) == projectStartCal.get(Calendar.YEAR))
                        && (currCal.get(Calendar.MONTH) > projectStartCal.get(Calendar.MONTH))
                    || (currCal.get(Calendar.YEAR) > projectStartCal.get(Calendar.YEAR))) {
                    project.setStatus(ProjectStatusEnum.IN_PROGRESS);
                    changeStatusProjects.add(project);
                    log.info(String.format("Added project %s to active today", project.getProjectId()));
                }
            }

            if (CollectionUtils.isNotEmpty(changeStatusProjects)) {
                projectRepository.saveAll(changeStatusProjects);
            }
        }
    }

//    @Scheduled(fixedRate = 1000)
    @Scheduled(cron = "0 0 0 ? * *")  // Run every day at 12:00 AM
    public void autoEndStatusForProject() {
        log.info("Cron job for ending status project is running...");
        List<Project> projectList = projectRepository.findAllByStatus(ProjectStatusEnum.IN_PROGRESS);

        if (CollectionUtils.isNotEmpty(projectList)) {
            Calendar currCal = Calendar.getInstance();
            Calendar projectTargetCal = Calendar.getInstance();

            List<Project> changeStatusProjects = new ArrayList<>();
            for (Project project : projectList) {
                // Check date to set ENDED status
                projectTargetCal.setTime(project.getTargetTime());
                if ((currCal.get(Calendar.YEAR) == projectTargetCal.get(Calendar.YEAR)
                        && (currCal.get(Calendar.MONTH) == projectTargetCal.get(Calendar.MONTH))
                        && (currCal.get(Calendar.DAY_OF_MONTH) >= projectTargetCal.get(Calendar.DAY_OF_MONTH)))) {
                    project.setStatus(ProjectStatusEnum.ENDED);
                    changeStatusProjects.add(project);
                    log.info(String.format("Added project %s to end today", project.getProjectId()));
                }
            }

            if (CollectionUtils.isNotEmpty(changeStatusProjects)) {
                projectRepository.saveAll(changeStatusProjects);
            }
        }
    }
}
