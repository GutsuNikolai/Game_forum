package com.example.gameforum.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Optional<ReviewEntity> findByGame_Slug(String slug);
    @EntityGraph(attributePaths = {"publisher"})
    Optional<ReviewEntity> findByGame_SlugAndStatus(String slug, ReviewStatus status);
}
