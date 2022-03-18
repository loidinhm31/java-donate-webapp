package com.donation.management.model;

import com.donation.common.core.enums.ProjectSearchColumn;
import com.donation.common.core.enums.SortType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import static com.donation.common.core.constant.Constants.DEFAULT_PAGE_SIZE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSearchFilter {
    private int startPage = 1;

    private int pageSize = DEFAULT_PAGE_SIZE;

    private ProjectSearchColumn sortBy;

    private SortType sortType;

    private ProjectSearchFilterData filters = new ProjectSearchFilterData();
}
