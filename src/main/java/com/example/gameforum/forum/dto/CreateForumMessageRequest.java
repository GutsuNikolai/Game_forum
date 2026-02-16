package com.example.gameforum.forum.dto;

import java.util.List;

public record CreateForumMessageRequest(
        String content,
        List<String> imageUrls
) {
}
