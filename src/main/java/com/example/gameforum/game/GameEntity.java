package com.example.gameforum.game;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "games")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "cover_url", nullable = false, columnDefinition = "text")
    private String coverUrl;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "rating_avg", nullable = false, precision = 4, scale = 2)
    private BigDecimal  ratingAvg;

    @Column(name = "rating_cnt", nullable = false)
    private Integer ratingCnt;
}
