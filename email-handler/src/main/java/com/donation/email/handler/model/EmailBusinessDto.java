package com.donation.email.handler.model;

import com.donation.common.core.enums.MailType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailBusinessDto {
    private MailType mailType;

    private String userName;

    public String firstName;

    private String lastName;

    private String phone;

    private String link;

    private String supportService;

    private String projectId;

    private String projectName;

    private Float amount;

    private String currency;
    
    private String transferCode;
    
    private String beforeDate;
    
    
}

