package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.AlbumRequest;
import com.jonathamjtm.gestaoartistas.dto.AlbumResponse;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AlbumResponse createAlbum(AlbumRequest request) {
        List<Artist> foundArtists = artistRepository.findAllById(request.getArtistIds());

        if (foundArtists.isEmpty()) {
            throw new RuntimeException("Nenhum artista válido encontrado com os IDs fornecidos.");
        }

        Album album = Album.builder()
                .title(request.getTitle())
                .releaseYear(request.getReleaseYear())
                .artists(new HashSet<>(foundArtists))
                .build();

        Album savedAlbum = albumRepository.save(album);

        AlbumResponse response = new AlbumResponse(savedAlbum);

        messagingTemplate.convertAndSend("/topic/albums", response);

        return response;
    }

    public List<AlbumResponse> findAll() {
        return albumRepository.findAll().stream()
                .map(AlbumResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlbum(Long id) {
        if (!albumRepository.existsById(id)) {
            throw new RuntimeException("Álbum não encontrado");
        }
        albumRepository.deleteById(id);
    }
}