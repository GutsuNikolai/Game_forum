package com.example.gameforum.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
    Optional<GameEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
