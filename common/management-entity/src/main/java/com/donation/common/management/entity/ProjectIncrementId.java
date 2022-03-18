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
@Table(name = "PROJECT_INCREMENT_ID", schema = DONATION_SCHEMA)
public class ProjectIncrementId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", unique = true, nullable = false)
    private Long projectId;
}
