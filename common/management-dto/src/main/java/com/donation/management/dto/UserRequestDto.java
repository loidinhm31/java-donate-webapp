package com.donation.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserRequestDto {

    @NotBlank(message = "Please fill out this field")
    private String username;

    @NotBlank(message = "Please fill out this field")
    private String email;

    @NotBlank(message = "Please fill out this field")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthdate;

    @NotBlank(message = "Please fill out this field")
    private String firstName;

    @NotBlank(message = "Please fill out this field")
    private String lastName;

    @Size(min = 10, max = 15, message = "Not valid phone number")
    private String phoneNumber;

    @Size(min = 8, max = 12, message = "Password must have 8 to 50 characters")
    private String password;
}
