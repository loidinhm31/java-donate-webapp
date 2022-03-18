package com.donation.common.management.repository;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.management.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String>, ProjectRepositoryCustom {

    @Query(value = "SELECT p.* FROM project p " +
            "WHERE p.project_id = :projectId " +
            "AND p.status = :status", nativeQuery = true)
    Project findByProjectIdAndStatus(String projectId, String status);


    List<Project> findAllByStatus(ProjectStatusEnum status);

    @Query(value = "SELECT count(p.*) " +
            "FROM project p " +
            "WHERE p.status = :status " +
            "AND EXTRACT (MONTH FROM p.updated_at) = :month " +
            "AND EXTRACT (YEAR FROM p.updated_at) = :year", nativeQuery = true)
    Integer countByMonthAndYearAndStatus(@Param("status") String status,
                                        @Param("month") Integer month,
                                        @Param("year") Integer year);

    @Query(value = "SELECT SUM(dt.amount) " +
            "FROM project p " +
            "INNER JOIN donation_time dt on p.project_id = dt.project_id " +
            "WHERE p.project_id = :projectId " +
            "AND dt.status = 'COMPLETE' " +
            "AND EXTRACT(MONTH FROM dt.updated_at) = :month " +
            "AND EXTRACT(YEAR FROM dt.updated_at) = :year", nativeQuery = true)
    Float sumDonationTimeByProjectAndMonthAndYear(String projectId, Integer month, Integer year);

    @Query(value = "SELECT SUM(dt.amount) " +
            "FROM project p " +
            "INNER JOIN donation_time dt on p.project_id = dt.project_id " +
            "WHERE p.project_id = :projectId " +
            "AND dt.status = 'COMPLETE' " +
            "AND dt.updated_at BETWEEN (:fromDate)\\:\\:date AND (:toDate)\\:\\:date", nativeQuery = true)
    Float sumDonationTimeByProjectInRangeTime(String projectId, String fromDate, String toDate);
}
