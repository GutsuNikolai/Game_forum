package com.example.gameforum.game.dto;

import com.example.gameforum.review.dto.ReviewView;

public record GameDetails(
        String slug,
        String title,
        String description,
        String coverUrl,
        double ratingAvg,
        int ratingCnt,
        ReviewView review
) {}
