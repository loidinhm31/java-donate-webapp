package com.donation.common.management.repository.projection;

import com.donation.common.core.enums.BeneficiaryType;
import com.donation.common.core.enums.ProjectStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProjectProjection {

    private String projectId;

    private String projectName;

    private String projectSummary;

    private String projectContent;

    private ProjectStatusEnum status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date targetTime;

    private Float targetMoney;

    private Float currentMoney;

    private Integer countExtend;

    private String beneficiaryPhoneNumber;

    private String beneficiaryName;

    private BeneficiaryType beneficiaryType;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date updatedAt;

    private String filePath;

    private Long countTotal;

    public ProjectProjection(Long countTotal) {
        this.countTotal = countTotal;
    }

    public ProjectProjection(String projectId, String projectName, String projectSummary, String projectContent, ProjectStatusEnum status, Date startTime, Date targetTime, Float targetMoney, Float currentMoney, Integer countExtend, String beneficiaryPhoneNumber, String beneficiaryName, BeneficiaryType beneficiaryType, Date createdAt, Date updatedAt) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectSummary = projectSummary;
        this.projectContent = projectContent;
        this.status = status;
        this.startTime = startTime;
        this.targetTime = targetTime;
        this.targetMoney = targetMoney;
        this.currentMoney = currentMoney;
        this.countExtend = countExtend;
        this.beneficiaryPhoneNumber = beneficiaryPhoneNumber;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryType = beneficiaryType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
