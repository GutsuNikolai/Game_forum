package com.example.gameforum.forum;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumTopicRepository extends JpaRepository<ForumTopicEntity, Long> {
    List<ForumTopicEntity> findByGameIdOrderByIdDesc(Long gameId);
    List<ForumTopicEntity> findByOrderByLastActivityAtDesc(Pageable pageable);
}
