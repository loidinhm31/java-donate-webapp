package com.donation.common.core.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum ProjectStatusEnum {
    CREATED("Created"),
    IN_PROGRESS("In-Progress"),
    ENDED("Ended"),
    CLOSED("Closed");

    @Getter
    private final String statusName;

    ProjectStatusEnum(String statusName) {
        this.statusName = statusName;
    }

    public static ProjectStatusEnum getEnum(String statusName) {

        switch (statusName) {
            case "CREATED":
                return CREATED;
            case "IN_PROGRESS":
                return IN_PROGRESS;
            case "ENDED":
                return ENDED;
            case "CLOSED":
                return CLOSED;
            default:
                return null;
        }
    }

}
