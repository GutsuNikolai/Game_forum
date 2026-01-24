package com.example.gameforum.game.dto;

public record GameListItem(
        String slug,
        String title,
        String coverUrl,
        double ratingAvg,
        int ratingCnt
) {}
