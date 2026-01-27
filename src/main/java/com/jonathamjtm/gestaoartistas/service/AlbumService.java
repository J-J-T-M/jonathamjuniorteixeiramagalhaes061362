package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.AlbumRequest;
import com.jonathamjtm.gestaoartistas.dto.AlbumResponse;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.AlbumImage;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.AlbumImageRepository;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final AlbumImageRepository albumImageRepository;
    private final StorageService storageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AlbumResponse createAlbum(AlbumRequest request) {
        List<Artist> foundArtists = artistRepository.findAllById(request.getArtistIds());
        if (foundArtists.isEmpty()) throw new RuntimeException("Artistas não encontrados");

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

    @Transactional
    public void uploadAlbumCover(Long albumId, MultipartFile file) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado"));

        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "." + extension;

        try {
            storageService.uploadFile(fileName, file.getInputStream(), file.getContentType(), file.getSize());

            AlbumImage image = AlbumImage.builder()
                    .fileName(fileName)
                    .contentType(file.getContentType())
                    .album(album)
                    .build();

            albumImageRepository.save(image);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar upload da imagem: " + e.getMessage());
        }
    }


    public List<AlbumResponse> findAll() {
        return albumRepository.findAll().stream()
                .map(album -> {
                    AlbumResponse response = new AlbumResponse(album);

                    if (album.getImages() != null) {
                        List<String> urls = album.getImages().stream()
                                .map(img -> storageService.getPresignedUrl(img.getFileName()))
                                .collect(Collectors.toList());
                        response.setCoverUrls(urls);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlbum(Long id) {
        if (!albumRepository.existsById(id)) throw new RuntimeException("Álbum não encontrado");
        albumRepository.deleteById(id);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
