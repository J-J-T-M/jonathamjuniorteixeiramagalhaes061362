-- 1. Cria a tabela pivô (Ponte entre Artistas e Álbuns)
CREATE TABLE artist_album (
                              artist_id BIGINT NOT NULL,
                              album_id BIGINT NOT NULL,

    -- Chave primária composta (evita duplicidade do mesmo par)
                              PRIMARY KEY (artist_id, album_id),

    -- Chaves Estrangeiras
                              CONSTRAINT fk_pivot_artist FOREIGN KEY (artist_id) REFERENCES artists (id) ON DELETE CASCADE,
                              CONSTRAINT fk_pivot_album FOREIGN KEY (album_id) REFERENCES albums (id) ON DELETE CASCADE
);

-- 2. Remove a coluna antiga da tabela albums (pois agora a relação fica na tabela pivô)
ALTER TABLE albums DROP COLUMN artist_id;