package com.donation.common.management.mapper;

import com.donation.common.management.entity.DonationTime;
import com.donation.management.dto.DonationTimeDto;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DonationTimeMapper {

    @Mappings({
            @Mapping(target = "transactionCode", ignore = true),
            @Mapping(target = "paymentMethod", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "project", ignore = true)
    })
    public abstract DonationTimeDto toDto(DonationTime donationTime);

    @Mappings({
            @Mapping(target = "transactionCode", ignore = true),
            @Mapping(target = "supporterName", ignore = true),
            @Mapping(target = "isDisplayName", ignore = true),
            @Mapping(target = "project.countExtend", ignore = true),
            @Mapping(target = "project.projectContent", ignore = true),
            @Mapping(target = "project.beneficiary", ignore = true)
    })
    public abstract DonationTimeDto donationTimeToDonatedProject(DonationTime donationTime);


    public DonationTimeDto donationTimeToDonator(DonationTime donationTime) {
        DonationTimeDto donator = toDto(donationTime);

        if (!donator.getIsDisplayName()) {
            donator.setSupporterName(encodeName(donationTime.getSupporterName()));
        }

        return donator;
    }

    private String encodeName(String name) {
        String encodedName = name;
        if (Objects.nonNull(name) && !name.equalsIgnoreCase("ANONYMOUS")) {
            if (name.length() > 3) {
                String encodingName = name.substring(3);
                encodedName =
                        encodedName + "*".repeat(encodingName.length());
                encodedName = name.substring(0, 3) + encodedName;
            } else {
                if (name.length() > 0) {
                    String encodingName = name.substring(1);

                    encodedName =
                            encodedName + "*".repeat(encodingName.length());

                    encodedName = name.charAt(0) + encodedName;
                }
            }
        }
        return encodedName;
    }
}
