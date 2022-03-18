package com.donation.data.handler.export;

import lombok.Getter;

public enum ExportEnum {
    USERNAME("Username", null),
    EMAIL("Email", null),
    FIRST_NAME("First Name", null),
    DATE_OF_BIRTH("Date Of Birth", null),
    LAST_LOGIN_TIME("Lasted Login Time", null),
    LAST_NAME("Last Name", null),
    PHONE_NUMBER("Phone Number", null),
    VERIFIED("Verified", null),
    ACTIVE_OR_DEACTIVATE("Active/ Deactivate (Enable/ Disable)", null);

    @Getter
    private final String header;

    @Getter
    private final String criteria;

    ExportEnum(String header, String criteria) {
        this.header = header;
        this.criteria = criteria;
    }
}
