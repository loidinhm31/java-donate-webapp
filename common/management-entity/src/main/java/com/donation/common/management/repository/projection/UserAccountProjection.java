package com.donation.common.management.repository.projection;

import com.donation.common.core.enums.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserAccountProjection implements Serializable {
    private UUID userId;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date birthdate;

    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date lastedLoginTime;

    private Boolean isActive;

    private Boolean isVerify;

    private AuthenticationType authenticationType;

    private Long countTotal;

    public UserAccountProjection(Long countTotal) {
        this.countTotal = countTotal;
    }

    public UserAccountProjection(UUID userId, String username, String email, String firstName, String lastName, Date birthdate, String phoneNumber, Date lastedLoginTime, Boolean isActive, Boolean isVerify, AuthenticationType authenticationType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.phoneNumber = phoneNumber;
        this.lastedLoginTime = lastedLoginTime;
        this.isActive = isActive;
        this.isVerify = isVerify;
        this.authenticationType = authenticationType;
    }
}
