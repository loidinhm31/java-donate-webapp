package com.donation.common.management.repository;

import com.donation.common.management.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {
}
