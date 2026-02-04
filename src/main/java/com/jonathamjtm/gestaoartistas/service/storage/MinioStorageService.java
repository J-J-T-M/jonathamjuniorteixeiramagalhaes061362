package com.jonathamjtm.gestaoartistas.service.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioStorageService implements FileStorageService, StorageService {

    private final MinioClient minioClient;
    private final MinioClient minioSignerClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url-expiration-minutes:30}")
    private Integer urlExpirationMinutes;

    public MinioStorageService(@Qualifier("minioClient") MinioClient minioClient,
                               @Qualifier("minioSignerClient") MinioClient minioSignerClient) {
        this.minioClient = minioClient;
        this.minioSignerClient = minioSignerClient;
    }

    @Override
    public List<String> upload(List<MultipartFile> files) {
        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String extension = getExtension(file.getOriginalFilename());
                String fileName = UUID.randomUUID() + "." + extension;
                this.uploadFile(fileName, file.getInputStream(), file.getContentType(), file.getSize());
                uploadedFileNames.add(fileName);
            } catch (Exception e) {
                log.error("Erro no arquivo: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Erro ao processar upload.");
            }
        }
        return uploadedFileNames;
    }

    @Override
    public void uploadFile(String fileName, InputStream inputStream, String contentType, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Upload MinIO concluído: {}", fileName);
        } catch (Exception e) {
            log.error("Erro MinIO Upload", e);
            throw new RuntimeException("Erro ao enviar para o storage.");
        }
    }

    @Override
    public String getPresignedUrl(String fileName) {
        try {
            String url = minioSignerClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .expiry(urlExpirationMinutes, TimeUnit.MINUTES)
                            .build()
            );

            log.info("URL Gerada para {}: {}", fileName, url);

            return url;
        } catch (Exception e) {
            log.error("Erro URL assinada", e);
            throw new RuntimeException("Erro ao gerar link temporário.");
        }
    }

    @Override
    public void delete(String fileName) {
        deleteFile(fileName);
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build()
            );
        } catch (Exception e) {
            log.error("Erro delete MinIO", e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}