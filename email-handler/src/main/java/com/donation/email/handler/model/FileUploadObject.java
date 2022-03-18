package com.donation.email.handler.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadObject {
    private String fileName;

    private byte[] fileData;
}
