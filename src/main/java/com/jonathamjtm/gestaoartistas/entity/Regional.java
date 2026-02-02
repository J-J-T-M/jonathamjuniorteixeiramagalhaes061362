package com.jonathamjtm.gestaoartistas.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "regionals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Regional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regional_id", nullable = false)
    private Long regionalId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}