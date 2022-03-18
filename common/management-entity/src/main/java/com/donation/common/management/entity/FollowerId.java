package com.donation.common.management.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FollowerId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "project_id")
    private String projectId;

}
