package com.donation.common.management.repository;

import com.donation.common.management.entity.FollowerProject;
import com.donation.common.management.entity.FollowerId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FollowerProjectRepository extends JpaRepository<FollowerProject, FollowerId> {

    @Query(value = "SELECT fp.* FROM follower_project fp " +
            "WHERE fp.user_id = :userId", nativeQuery = true)
    Page<FollowerProject> findByUserId(UUID userId, Pageable pageable);

    @Query(value = "SELECT fp.* FROM follower_project fp " +
            "WHERE fp.project_id = :projectId " +
            "AND fp.user_id = :userId", nativeQuery = true)
    FollowerProject findByProjectIdAndUserId(@Param("projectId") String projectId,
                                             @Param("userId") UUID userId);

    @Query(value = "SELECT fp.* " +
            "FROM follower_project fp " +
            "LEFT JOIN user_account ua on fp.user_id = ua.user_id " +
            "INNER JOIN user_detail ud on fp.user_id = ud.user_id " +
            "WHERE fp.project_id = :projectId " +
            "AND (ua.username = :userIdentifier " +
            "    OR ud.email = :userIdentifier)", nativeQuery = true)
    FollowerProject findByProjectIdAndUserIdentifier(@Param("projectId") String projectId,
                                                     @Param("userIdentifier") String userIdentifier);

    @Query(value = "SELECT fp.* FROM follower_project fp " +
            "WHERE fp.project_id = :projectId", nativeQuery = true)
    List<FollowerProject> findAllByProjectId(String projectId);
}
