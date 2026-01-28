package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.ArtistRequest;
import com.jonathamjtm.gestaoartistas.dto.ArtistResponse;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.repository.specs.ArtistSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ArtistResponse createArtist(ArtistRequest request) {
        Artist artist = Artist.builder()
                .name(request.getName())
                .build();

        Artist savedArtist = artistRepository.save(artist);
        ArtistResponse response = new ArtistResponse(savedArtist);

        messagingTemplate.convertAndSend("/topic/artists", response);

        return response;
    }

    public List<ArtistResponse> findAll(String name, LocalDateTime createdAfter, String sortDirection) {

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, "name");

        Specification<Artist> spec = ArtistSpecs.filterBy(name, createdAfter);

        List<Artist> artists = artistRepository.findAll(spec, sort);

        return artists.stream().map(ArtistResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public void deleteArtist(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new RuntimeException("Artista não encontrado para exclusão");
        }
        artistRepository.deleteById(id);
    }
}