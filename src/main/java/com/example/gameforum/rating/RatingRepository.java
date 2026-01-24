package com.example.gameforum.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<RatingEntity, Long> {

    Optional<RatingEntity> findByGame_SlugAndUser_Username(String slug, String username);

    @Query("""
        select coalesce(avg(r.value), 0), count(r)
        from RatingEntity r
        where r.game.id = :gameId
    """)
    List<Object[]> getAvgAndCount(@Param("gameId") long gameId);
}
