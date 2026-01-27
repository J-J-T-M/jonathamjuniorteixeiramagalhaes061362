CREATE TABLE album_images
(
    id           BIGSERIAL PRIMARY KEY,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    album_id     BIGINT       NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_album_images_album
        FOREIGN KEY (album_id)
            REFERENCES albums (id)
            ON DELETE CASCADE
);