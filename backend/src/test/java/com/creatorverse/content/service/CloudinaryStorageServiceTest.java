package com.creatorverse.content.service;

import com.cloudinary.Cloudinary;
import com.creatorverse.common.exception.UploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CloudinaryStorageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @InjectMocks
    private CloudinaryStorageServiceImpl storageService;

    @Test
    void uploadFile_EmptyFile_ThrowsUploadException() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        UploadException exception = assertThrows(UploadException.class, () -> storageService.uploadFile(file));
        assertTrue(exception.getMessage().contains("Cannot upload empty file"));
    }

    @Test
    void uploadFile_UnsupportedMimeType_ThrowsUploadException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        UploadException exception = assertThrows(UploadException.class, () -> storageService.uploadFile(file));
        assertTrue(exception.getMessage().contains("Unsupported file type"));
    }
}
