-- =========================================================================
-- CARGA INICIAL (POPULAR TABELAS) - CONFORME EDITAL SEPLAG/MT
-- =========================================================================

-- 1. Cria o Usuário Admin Padrão para os testes
INSERT INTO users (full_name, email, password, role)
VALUES ('Administrador SEPLAG', 'admin@seplag.mt.gov.br', '$2a$10$404E635266556A586E3272357538782F413F4428472B4B62506453', 'ADMIN'); -- Senha mockada

-- 2. Insere os Artistas com a tipagem exigida (Cantor/Banda)
INSERT INTO artists (id, name, artist_type) VALUES (1, 'Serj Tankian', 'SINGER');
INSERT INTO artists (id, name, artist_type) VALUES (2, 'Mike Shinoda', 'SINGER');
INSERT INTO artists (id, name, artist_type) VALUES (3, 'Michel Teló', 'SINGER');
INSERT INTO artists (id, name, artist_type) VALUES (4, 'Guns N'' Roses', 'BAND');

-- 3. Insere os Álbuns citados no edital
INSERT INTO albums (id, title, release_year) VALUES (1, 'Harakiri', 2012);
INSERT INTO albums (id, title, release_year) VALUES (2, 'The Rising Tied', 2005);
INSERT INTO albums (id, title, release_year) VALUES (3, 'Bem Sertanejo', 2014);
INSERT INTO albums (id, title, release_year) VALUES (4, 'Use Your Illusion I', 1991);

-- 4. Vincula Artistas aos Álbuns (Tabela Pivô)
INSERT INTO artist_album (artist_id, album_id) VALUES (1, 1); -- Serj Tankian -> Harakiri
INSERT INTO artist_album (artist_id, album_id) VALUES (2, 2); -- Mike Shinoda -> The Rising Tied
INSERT INTO artist_album (artist_id, album_id) VALUES (3, 3); -- Michel Teló -> Bem Sertanejo
INSERT INTO artist_album (artist_id, album_id) VALUES (4, 4); -- Guns N' Roses -> Use Your Illusion I

-- 5. Ajusta as Sequences do PostgreSQL para evitar erro de ID duplicado nas próximas inserções
ALTER SEQUENCE artists_id_seq RESTART WITH 5;
ALTER SEQUENCE albums_id_seq RESTART WITH 5;