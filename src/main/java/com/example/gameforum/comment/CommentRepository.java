package com.example.gameforum.comment;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<CommentEntity> findByGame_SlugAndDeletedFalse(String slug, Pageable pageable);
}
