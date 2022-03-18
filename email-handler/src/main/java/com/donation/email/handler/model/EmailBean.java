package com.donation.email.handler.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailBean {
    private String from;

    private String to;

    private String cc;

    private String emailSubject;

    private String emailBody;

    private List<FileUploadObject> uploads;
}
