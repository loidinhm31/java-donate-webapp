package com.donation.common.management.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BENEFICIARY_INCREMENT_ID", schema = DONATION_SCHEMA)
public class BeneficiaryIncrementId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "beneficiary_id", unique = true, nullable = false)
    private Long beneficiaryId;
}
