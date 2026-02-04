package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.request.AlbumRequest;
import com.jonathamjtm.gestaoartistas.dto.response.AlbumResponse;
import com.jonathamjtm.gestaoartistas.entity.Album;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.AlbumImageRepository;
import com.jonathamjtm.gestaoartistas.repository.AlbumRepository;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.service.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @InjectMocks
    private AlbumService albumService;

    @Mock private AlbumRepository albumRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private AlbumImageRepository albumImageRepository;
    @Mock private FileStorageService fileStorageService;

    @Mock private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("Deve notificar o tópico /topic/albums ao criar um novo álbum")
    void shouldNotifyWebSocketClientsOnCreate() {
        // ARRANGE
        AlbumRequest request = new AlbumRequest();
        request.setTitle("Novo Album");
        request.setReleaseYear(2025);
        request.setArtistIds(List.of(1L));

        Artist artist = new Artist();
        artist.setId(1L);

        when(artistRepository.findAllById(any())).thenReturn(List.of(artist));
        when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> {
            Album album = invocation.getArgument(0);
            album.setId(100L);
            return album;
        });

        // ACT
        AlbumResponse response = albumService.createAlbum(request);

        // ASSERT
        assertNotNull(response);

        verify(messagingTemplate).convertAndSend(eq("/topic/albums"), any(AlbumResponse.class));
    }
}