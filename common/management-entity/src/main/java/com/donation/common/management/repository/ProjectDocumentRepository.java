package com.donation.common.management.repository;

import com.donation.common.management.entity.ProjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectDocumentRepository extends JpaRepository<ProjectDocument, Long> {

    @Query(value = "SELECT pd.* " +
            "FROM project_doc pd " +
            "WHERE pd.project_id = :projectId", nativeQuery = true)
    List<ProjectDocument> findByProjectId(@Param("projectId") String projectId);

    @Query(value = "SELECT pd.* " +
            "FROM project_doc pd " +
            "WHERE pd.project_id = :projectId " +
            "LIMIT 1", nativeQuery = true)
    ProjectDocument findFirstByProjectId(@Param("projectId") String projectId);
}
