package com.example.gameforum.forum.dto;

import java.util.List;

public record ForumMessageView(
        Long id,
        String author,
        String avatarColor,
        String date,
        String content,
        List<String> imageUrls,
        int likes,
        int replies
) {
}
