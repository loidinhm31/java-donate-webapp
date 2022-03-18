package com.donation.common.core.enums;

import lombok.Getter;

public enum RoleEnum {
    ADMIN("Admin"),
    CLIENT("Client");


    @Getter
    private final String roleName;

    RoleEnum(String roleName) {
        this.roleName = roleName;
    };
}
