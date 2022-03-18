package com.donation.data.handler.cronjob;

import com.donation.common.management.repository.ProjectIncrementIdRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
public class ResetIdJob {
    @Autowired
    private ProjectIncrementIdRepository projectIncrementIdRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 0 1 1 *")
    public void resetSequence() {
        log.info("Drop all rows in project increment id table");
        projectIncrementIdRepository.deleteAll();
        log.info("Reset sequence of project increment id table");
        String seqProjectId = "ALTER SEQUENCE donation.project_increment_id_seq RESTART WITH 1";
        jdbcTemplate.execute(seqProjectId);

        log.info("Exiting reset sequence");
    }
}
