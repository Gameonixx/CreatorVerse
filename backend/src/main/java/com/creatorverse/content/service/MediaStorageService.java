package com.creatorverse.content.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MediaStorageService {
    
    /**
     * Uploads a file to the storage provider.
     * @param file the MultipartFile to upload
     * @return a map containing metadata like "url", "secure_url", "public_id", etc.
     */
    Map<String, Object> uploadFile(MultipartFile file);
    
    /**
     * Deletes a file from the storage provider.
     * @param publicId the ID of the file to delete
     */
    void deleteFile(String publicId);
}
