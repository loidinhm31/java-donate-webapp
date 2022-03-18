package com.donation.common.management.converter;

import com.donation.common.core.enums.RoleEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RoleEnumConverter implements AttributeConverter<RoleEnum, String> {

    @Override
    public String convertToDatabaseColumn(RoleEnum attribute) {
        return attribute.toString();
    }

    @Override
    public RoleEnum convertToEntityAttribute(String dbData) {
        try {
            return RoleEnum.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
