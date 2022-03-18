package com.donation.management.dto;

import com.donation.common.core.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleDto implements Serializable {

    private UUID roleId;

    private RoleEnum roleCode;

    private String roleName;

    private Boolean check;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleDto roleDto = (RoleDto) o;
        return roleCode == roleDto.roleCode && Objects.equals(roleName, roleDto.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleCode, roleName);
    }
}
