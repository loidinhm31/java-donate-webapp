package com.donation.common.management.entity;

import com.donation.common.core.enums.BeneficiaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;

@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "beneficiary", schema = DONATION_SCHEMA)
public class Beneficiary extends BaseEntity<String> {
    @Id
    @Column(name = "beneficiary_id", unique = true, nullable = false)
    private String beneficiaryId;

    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @Column(name = "beneficiary_phone_number")
    private String beneficiaryPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficiary_type")
    private BeneficiaryType beneficiaryType;

    @Column(name = "is_receive")
    private Boolean isReceive;

    @OneToMany(mappedBy = "beneficiary", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Project> projects = new ArrayList<>();


}
