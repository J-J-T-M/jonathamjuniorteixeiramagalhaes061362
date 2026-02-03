package com.jonathamjtm.gestaoartistas.service;

import com.jonathamjtm.gestaoartistas.dto.request.ArtistRequest;
import com.jonathamjtm.gestaoartistas.dto.response.ArtistResponse;
import com.jonathamjtm.gestaoartistas.entity.Artist;
import com.jonathamjtm.gestaoartistas.exception.ResourceNotFoundException;
import com.jonathamjtm.gestaoartistas.repository.ArtistRepository;
import com.jonathamjtm.gestaoartistas.repository.specs.ArtistSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Transactional
    public ArtistResponse createArtist(ArtistRequest request) {
        Artist artist = new Artist();
        artist.setName(request.getName());
        artist.setType(request.getType());

        artist = artistRepository.save(artist);
        return mapToResponse(artist);
    }

    @Transactional
    public ArtistResponse updateArtist(Long id, ArtistRequest request) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com ID: " + id));

        artist.setName(request.getName());
        artist.setType(request.getType());

        artist = artistRepository.save(artist);
        return mapToResponse(artist);
    }

    @Transactional
    public void deleteArtist(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Artista não encontrado com ID: " + id);
        }
        artistRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> findAll(String name, LocalDateTime createdAfter, String sortDirection) {
        Specification<Artist> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and(ArtistSpecs.nameContainsIgnoreCase(name));
        }

        if (createdAfter != null) {
            spec = spec.and(ArtistSpecs.createdAfter(createdAfter));
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "name");
        List<Artist> artists = artistRepository.findAll(spec, sort);

        return artists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArtistResponse findById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista não encontrado com ID: " + id));
        return mapToResponse(artist);
    }

    private ArtistResponse mapToResponse(Artist artist) {
        return new ArtistResponse(artist);
    }
}