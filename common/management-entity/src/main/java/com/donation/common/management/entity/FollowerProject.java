package com.donation.common.management.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "follower_project", schema = DONATION_SCHEMA)
public class FollowerProject {

    @EmbeddedId
    private FollowerId followerId;

    @ManyToOne
    @MapsId(value = "userId")
    @JoinColumn(name = "user_id")
    private UserAccountDetail userAccountDetail;

    @ManyToOne
    @MapsId(value = "projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "is_notify")
    private Boolean isNotify;

}
