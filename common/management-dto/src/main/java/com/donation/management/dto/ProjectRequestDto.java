package com.donation.management.dto;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRequestDto {

    private String projectId;

    private String projectName;

    private ProjectStatusEnum status;

    private String projectSummary;

    private String projectContent;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date targetTime;

    @NumberFormat(pattern = "#.#")
    private Float targetMoney;

    private String beneficiaryId;

    private String filePath;

}
