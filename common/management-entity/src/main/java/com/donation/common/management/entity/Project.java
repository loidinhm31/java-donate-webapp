package com.donation.common.management.entity;

import com.donation.common.core.enums.ProjectStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "project", schema = DONATION_SCHEMA)
public class Project extends BaseEntity<String> {

    @Id
    @Column(name = "project_id", unique = true, nullable = false)
    private String projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_summary")
    private String projectSummary;

    @Column(name = "project_content", columnDefinition = "LONGTEXT")
    private String projectContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProjectStatusEnum status;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "target_time")
    private Date targetTime;

    @Column(name = "target_money")
    private Float targetMoney;

    @Column(name = "current_money")
    private Float currentMoney = 0F;

    @Column(name = "count_extend")
    private Integer countExtend;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<DonationTime> donationTime = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private UserAccountDetail creator;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<FollowerProject> followerProjects;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProjectDocument> projectDocuments = new ArrayList<>();
}
