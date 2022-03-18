package com.donation.common.management.entity;

import com.donation.common.core.enums.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "user_detail", schema = DONATION_SCHEMA)
public class UserAccountDetail extends BaseEntity<String> {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "birthdate")
    private Date birthdate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "reset_token")
    private String resetToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_type")
    private AuthenticationType authenticationType;

    @Column(name = "lasted_login_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastedLoginTime;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserAccount userAccount;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "userAccountDetail", fetch = FetchType.LAZY)
    private List<FollowerProject> followerProjects;
}
