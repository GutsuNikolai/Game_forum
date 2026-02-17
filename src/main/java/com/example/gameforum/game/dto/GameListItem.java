package com.example.gameforum.game.dto;

public record GameListItem(
        Long id,
        String slug,
        String title,
        String description,
        String coverUrl,
        double ratingAvg,
        int ratingCnt
) {}
