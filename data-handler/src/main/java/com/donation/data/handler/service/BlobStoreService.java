package com.donation.data.handler.service;

import org.springframework.web.multipart.MultipartFile;

public interface BlobStoreService {

    String uploadFile(MultipartFile file);

    void deleteFile(String filePath);
}
