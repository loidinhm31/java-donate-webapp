package com.donation.common.management.repository;

import com.donation.common.management.entity.UserAccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAccountDetailRepository extends JpaRepository<UserAccountDetail, UUID>, UserAccountDetailRepositoryCustom {

    UserAccountDetail findByEmail(String email);

    UserAccountDetail findByVerificationCode(String verificationCode);

    UserAccountDetail findByResetToken(String resetToken);

    @Query(value = "SELECT ud.* " +
            "FROM user_detail ud " +
            "INNER JOIN user_account ua on ua.user_id = ud.user_id " +
            "WHERE ud.email = :identifier " +
            "OR ua.username = :identifier", nativeQuery = true)
    UserAccountDetail findByEmailOrUsername(@Param("identifier") String identifier);
}
