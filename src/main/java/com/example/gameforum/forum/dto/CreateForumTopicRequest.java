package com.example.gameforum.forum.dto;

public record CreateForumTopicRequest(
        String title,
        String description,
        String icon
) {
}
