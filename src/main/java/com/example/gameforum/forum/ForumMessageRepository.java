package com.example.gameforum.forum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumMessageRepository extends JpaRepository<ForumMessageEntity, Long> {
    List<ForumMessageEntity> findByTopicIdOrderByIdAsc(Long topicId);
    long countByAuthor(String author);
}
