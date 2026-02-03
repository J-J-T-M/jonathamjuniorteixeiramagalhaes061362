package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.request.AlbumRequest;
import com.jonathamjtm.gestaoartistas.dto.response.AlbumResponse;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.AlbumImage;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.exception.ResourceNotFoundException;
import com.jonathamjtm.gestaoartistas.repository.AlbumImageRepository;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.repository.specs.AlbumSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final AlbumImageRepository albumImageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AlbumResponse createAlbum(AlbumRequest request) {
        Album album = new Album();
        album.setTitle(request.getTitle());
        album.setReleaseYear(request.getReleaseYear());

        updateArtists(album, request.getArtistIds());

        album = albumRepository.save(album);
        AlbumResponse response = mapToResponse(album);

        // Notificação WebSocket
        messagingTemplate.convertAndSend("/topic/albums", response);

        return response;
    }

    // MÉTODO NOVO: Necessário para o PUT do Controller
    @Transactional
    public AlbumResponse updateAlbum(Long id, AlbumRequest request) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com ID: " + id));

        album.setTitle(request.getTitle());
        album.setReleaseYear(request.getReleaseYear());

        updateArtists(album, request.getArtistIds());

        album = albumRepository.save(album);
        return mapToResponse(album);
    }

    @Transactional(readOnly = true)
    public Page<AlbumResponse> searchAlbums(String title, Integer releaseYear, Long artistId, Pageable pageable) {
        Specification<Album> spec = Specification.where(null);

        if (title != null && !title.isBlank()) {
            spec = spec.and(AlbumSpecs.titleContainsIgnoreCase(title));
        }
        if (releaseYear != null) {
            spec = spec.and(AlbumSpecs.releaseYearGreaterThanEqual(releaseYear));
        }
        if (artistId != null) {
            spec = spec.and(AlbumSpecs.hasArtistId(artistId));
        }

        return albumRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com ID: " + id));
        return mapToResponse(album);
    }

    @Transactional
    public void deleteAlbum(Long id) {
        if (!albumRepository.existsById(id)) {
            throw new ResourceNotFoundException("Álbum não encontrado com ID: " + id);
        }
        albumRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String findImageNameByAlbumId(Long albumId) {
        AlbumImage albumImage = albumImageRepository.findByAlbumId(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada para o álbum ID: " + albumId));
        return albumImage.getFileName();
    }

    private void updateArtists(Album album, List<Long> artistIds) {
        if (artistIds != null && !artistIds.isEmpty()) {
            List<Artist> artists = artistRepository.findAllById(artistIds);
            if (artists.isEmpty()) {
                throw new ResourceNotFoundException("Nenhum artista válido encontrado para os IDs fornecidos.");
            }
            album.setArtists(new HashSet<>(artists));
        }
    }

    private AlbumResponse mapToResponse(Album album) {
        return new AlbumResponse(album);
    }
}