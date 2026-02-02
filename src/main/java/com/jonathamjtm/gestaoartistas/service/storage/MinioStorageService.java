package com.jonathamjtm.gestaoartistas.service.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements FileStorageService, StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url-expiration-minutes:30}")
    private Integer urlExpirationMinutes;


    @Override
    public String upload(MultipartFile file) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + "." + extension;

            this.uploadFile(fileName, file.getInputStream(), file.getContentType(), file.getSize());

            return fileName;
        } catch (Exception e) {
            log.error("Erro ao processar MultipartFile", e);
            throw new RuntimeException("Erro ao processar upload de imagem.");
        }
    }

    @Override
    public void delete(String fileName) {
        this.deleteFile(fileName);
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
            log.info("Upload concluído: {}", fileName);
        } catch (Exception e) {
            log.error("Erro ao enviar para MinIO", e);
            throw new RuntimeException("Erro ao fazer upload para o storage.");
        }
    }

    @Override
    public String getPresignedUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .expiry(urlExpirationMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erro ao gerar URL assinada", e);
            throw new RuntimeException("Erro ao gerar link temporário.");
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("Arquivo deletado: {}", fileName);
        } catch (Exception e) {
            log.error("Erro ao deletar do MinIO", e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}