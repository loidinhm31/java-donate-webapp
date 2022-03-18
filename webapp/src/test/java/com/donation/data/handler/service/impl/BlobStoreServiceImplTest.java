package com.donation.data.handler.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.donation.data.handler.service.BlobStoreService;
import com.donation.data.handler.service.impl.BlobStoreServiceImpl;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BlobStoreServiceImplTest {
    @InjectMocks
    private BlobStoreService blobStoreServiceTest = new BlobStoreServiceImpl();

    @Mock
    private Cloudinary cloudinary = new Cloudinary();

    @Test
    public void testUploadFile() {
        MultipartFile multipartFile =
                new MockMultipartFile("file", "orig", null, "image".getBytes());

        blobStoreServiceTest.uploadFile(multipartFile);

        verify(cloudinary).uploader();
    }

    @Test
    public void testDeleteFile() {
        MultipartFile multipartFile =
                new MockMultipartFile("file", "orig", null, "image".getBytes());

        blobStoreServiceTest.deleteFile("/PATH/image.jpg");

        verify(cloudinary).uploader();
    }
}
