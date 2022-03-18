package com.donation.management.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class ProjectSearchFilterData {

    private List<String> phoneNumbers;

    private List<String> projectCodes;

    private List<String> projectStatuses;
}
