package com.jonathamjtm.gestaoartistas.config.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class MinioConfiguration {

    @Value("${minio.url}")
    private String internalUrl;

    @Value("${minio.external-url}")
    private String externalUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    @Primary
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(internalUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean(name = "minioSignerClient")
    public MinioClient minioSignerClient() {
        return MinioClient.builder()
                .endpoint(externalUrl)
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .build();
    }

    @Bean
    public CommandLineRunner ensureBucketExists(MinioClient minioClient) {
        return args -> {
            try {
                log.info("Verificando existência do bucket: {}", bucketName);
                boolean found = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build()
                );

                if (!found) {
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucketName).build()
                    );
                    log.info("Bucket '{}' criado (PRIVADO) com sucesso!", bucketName);
                } else {
                    log.info("Bucket '{}' já existe.", bucketName);
                }
            } catch (Exception e) {
                log.error("FALHA GRAVE: Não foi possível conectar ao MinIO.", e);
            }
        };
    }
}