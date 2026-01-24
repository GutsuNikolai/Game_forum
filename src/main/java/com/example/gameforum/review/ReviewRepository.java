package com.example.gameforum.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Optional<ReviewEntity> findByGame_Slug(String slug);
    Optional<ReviewEntity> findByGame_SlugAndStatus(String slug, ReviewStatus status);
}
