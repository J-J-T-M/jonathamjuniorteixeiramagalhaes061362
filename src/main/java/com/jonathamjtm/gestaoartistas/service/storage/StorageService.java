package com.jonathamjtm.gestaoartistas.service.storage;

import java.io.InputStream;

public interface StorageService {

    void uploadFile(String fileName, InputStream inputStream, String contentType, long size);

    String getPresignedUrl(String fileName);

    void deleteFile(String fileName);
}