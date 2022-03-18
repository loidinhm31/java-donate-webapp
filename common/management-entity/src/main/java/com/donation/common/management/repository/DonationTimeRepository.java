package com.donation.common.management.repository;

import com.donation.common.management.entity.DonationTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DonationTimeRepository extends JpaRepository<DonationTime, String> {

    @Query(value = "SELECT COUNT(dt.*) FROM donation_time dt " +
            "WHERE project_id = :projectId " +
            "AND dt.status = :status " +
            "AND dt.amount IS NOT NULL", nativeQuery = true)
    Long countDonationTimeByProjectAndStatus(String projectId, String status);

    @Query(value = "SELECT dt.* FROM donation_time dt " +
            "WHERE dt.project_id = :projectId " +
            "AND dt.status = :status " +
            "AND dt.amount IS NOT NULL", nativeQuery = true)
    Page<DonationTime> findDonationTimeByProjectAndStatus(String projectId, String status, Pageable pageable);

    @Query(value = "SELECT dt.* FROM donation_time dt " +
            "WHERE id = :id " +
            "AND project_id = :projectId", nativeQuery = true)
    DonationTime findByIdAndProjectId(String id, String projectId);

    @Query(value = "SELECT dt.* FROM donation_time dt " +
            "WHERE dt.user_id = :userId", nativeQuery = true)
    Page<DonationTime> findDonationTimeByUser(UUID userId, Pageable pageable);

    DonationTime findByTransactionCode(String transactionCode);
}
