package com.donation.management.dto;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.NumberFormat;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProjectDto extends BaseDto {
    private String projectId;

    private String projectName;

    private String projectSummary;

    private String projectContent;

    private ProjectStatusEnum status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date targetTime;

    private Float targetMoney;

    private Float currentMoney;

    private Integer countExtend;

    private BeneficiaryDto beneficiary;

    private String filePath;

    private Long donatedCount;
}
