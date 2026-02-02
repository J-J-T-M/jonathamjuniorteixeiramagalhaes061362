package com.jonathamjtm.gestaoartistas.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "albums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "artists")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private Integer releaseYear;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "artist_album",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @Builder.Default
    private Set<Artist> artists = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}