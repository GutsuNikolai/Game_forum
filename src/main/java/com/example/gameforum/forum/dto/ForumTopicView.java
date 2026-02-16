package com.example.gameforum.forum.dto;

public record ForumTopicView(
        Long id,
        Long gameId,
        String title,
        String description,
        String author,
        Integer replies,
        Integer views,
        String lastActivity,
        String icon
) {
}
