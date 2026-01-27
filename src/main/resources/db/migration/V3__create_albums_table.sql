CREATE TABLE albums (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        release_year INTEGER,
                        artist_id BIGINT NOT NULL, -- A Chave Estrangeira
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Define que artist_id aponta para o id da tabela artists
                        CONSTRAINT fk_albums_artist
                            FOREIGN KEY (artist_id)
                                REFERENCES artists (id)
                                ON DELETE CASCADE -- Se apagar o artista, apaga os álbuns dele
);