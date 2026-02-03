package com.jonathamjtm.gestaoartistas.service.storage;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileStorageService {

    List<String> upload(List<MultipartFile> files);

    String getPresignedUrl(String fileName);

    void delete(String fileName);
}