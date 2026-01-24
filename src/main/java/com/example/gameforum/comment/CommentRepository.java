package com.example.gameforum.comment;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    Page<CommentEntity> findByGame_SlugAndDeletedFalse(String slug, Pageable pageable);
}
