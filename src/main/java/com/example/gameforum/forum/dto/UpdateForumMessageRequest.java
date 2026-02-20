package com.example.gameforum.forum.dto;

import java.util.List;

public record UpdateForumMessageRequest(
        String content,
        List<String> imageUrls
) {
}
