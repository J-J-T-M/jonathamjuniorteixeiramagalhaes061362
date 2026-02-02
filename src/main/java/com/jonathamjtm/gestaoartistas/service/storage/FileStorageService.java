package com.jonathamjtm.gestaoartistas.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String upload(MultipartFile file);

    String getPresignedUrl(String fileName);

    void delete(String fileName);
}