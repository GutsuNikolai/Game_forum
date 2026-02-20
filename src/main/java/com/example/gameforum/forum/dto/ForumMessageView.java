package com.example.gameforum.forum.dto;

import java.util.List;

public record ForumMessageView(
        Long id,
        String author,
        String avatarColor,
        String date,
        String content,
        List<String> imageUrls,
        Long parentMessageId,
        Long quotedMessageId,
        String quotedAuthor,
        String quotedPreview,
        int likes,
        int dislikes,
        int replies,
        boolean edited
) {
}
