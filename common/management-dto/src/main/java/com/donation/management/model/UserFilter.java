package com.donation.management.model;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.enums.SortType;
import com.donation.common.core.enums.UserSortBy;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFilter implements Serializable {
    private int startPage;

    private int pageSize;

    private UserSortBy sortBy;

    private SortType sortType;

    private List<RoleEnum> roleEnums;

    private Boolean isVerify;

    private Boolean isActive;

    private String searchKey;

    private String lastedLoginTime;

    private String option;
}
