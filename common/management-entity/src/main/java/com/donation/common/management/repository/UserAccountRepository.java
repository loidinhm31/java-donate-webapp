package com.donation.common.management.repository;

import com.donation.common.management.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;


public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
	UserAccount findByUsername(String username);

	@Query(value = "SELECT MAX(ua.username)" +
			"FROM user_account ua " +
			"WHERE ua.username LIKE %:username%", nativeQuery = true)
	List<String> findByMaxIdUsernameByUsername(@Param("username") String username);
}
