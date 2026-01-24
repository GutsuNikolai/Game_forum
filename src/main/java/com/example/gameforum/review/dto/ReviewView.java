package com.example.gameforum.review.dto;

import com.example.gameforum.review.ReviewStatus;

import java.time.OffsetDateTime;

public record ReviewView(
        String title,
        String content,
        ReviewStatus status,
        String publisherUsername,
        OffsetDateTime updatedAt
) {}
