package com.donation.data.handler.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import com.donation.data.handler.service.BlobStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class BlobStoreServiceImpl implements BlobStoreService {

    @Value("${cloudinary.folder}")
    private String folderPath;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) {
        log.info("Uploading file...");

        File uploadedFile = null;
        try {
            String fileObject = folderPath + System.currentTimeMillis();

            uploadedFile = convertMultiPartToFile(file);

            Map uploadResult = cloudinary
                    .uploader()
                    .upload(uploadedFile,
                            ObjectUtils.asMap("public_id", fileObject));

            log.info("Complete upload file {}", fileObject);
            return fileObject + "." + uploadResult.get("format");
        } catch (Exception e) {
            log.error("Upload file exception", e);
        } finally {
            if (Objects.nonNull(uploadedFile)
                    && uploadedFile.exists()) {
                uploadedFile.delete();
            }
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void deleteFile(String filePath) {
        String objectKey = filePath.split("\\.")[0];

        try {
            log.info("Deleting object key {}...", objectKey);
            cloudinary
                    .uploader()
                    .destroy(objectKey,
                            ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Delete file exception", e);
        }
        log.info("Complete delete file");
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
            return convFile;
        }
    }
}
