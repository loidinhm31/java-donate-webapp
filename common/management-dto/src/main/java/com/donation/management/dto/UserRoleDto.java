package com.donation.management.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRoleDto {

    private UUID id;
    private UUID userId;
    private RoleDto role;
}
