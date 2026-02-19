package com.example.gameforum.forum.dto;

public record ForumMessageReactionView(
        Long messageId,
        int likes,
        int dislikes
) {
}

