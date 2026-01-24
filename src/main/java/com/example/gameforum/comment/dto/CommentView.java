package com.example.gameforum.comment.dto;

import java.time.OffsetDateTime;

public record CommentView(
        long id,
        String username,
        String content,
        OffsetDateTime createdAt
) {}
