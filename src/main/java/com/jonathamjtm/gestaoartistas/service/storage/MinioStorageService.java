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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
                validateImageFile(file);

                String extension = getExtension(file.getOriginalFilename());
                String fileName = UUID.randomUUID() + "." + extension;

                this.uploadFile(fileName, file.getInputStream(), file.getContentType(), file.getSize());
                uploadedFileNames.add(fileName);

            } catch (Exception e) {
                log.error("Falha crítica no upload do arquivo '{}'. Erro: {}", file.getOriginalFilename(), e.getMessage(), e);
                throw new RuntimeException("Erro ao processar upload do arquivo: " + file.getOriginalFilename(), e);
            }
        }
        return uploadedFileNames;
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Arquivo vazio não permitido.");

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            if (is.read(header) < 4) {
                throw new IllegalArgumentException("Arquivo corrompido ou muito pequeno.");
            }

            boolean isPng = (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47);
            boolean isJpeg = (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF);

            if (!isPng && !isJpeg) {
                throw new IllegalArgumentException("Formato de arquivo inválido/inseguro. Apenas PNG e JPG reais são aceitos.");
            }
        }
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
            log.error("Erro de comunicação com MinIO no upload", e);
            throw new RuntimeException("Erro de infraestrutura no storage.", e);
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
            return url;
        } catch (Exception e) {
            log.error("Erro ao gerar URL assinada para: {}", fileName, e);
            throw new RuntimeException("Erro ao gerar link temporário.", e);
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
            log.error("Falha ao deletar arquivo '{}' do MinIO", fileName, e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}