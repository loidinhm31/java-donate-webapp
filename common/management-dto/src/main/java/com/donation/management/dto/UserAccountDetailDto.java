package com.donation.management.dto;

import com.donation.common.core.enums.AuthenticationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAccountDetailDto extends BaseDto {

    private UUID userId;

    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date birthdate;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private AuthenticationType authenticationType;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date lastedLoginTime;

    private Boolean isActive;

    private Boolean isVerify;

    private String password;

    private UserAccountDto userAccount;

}
