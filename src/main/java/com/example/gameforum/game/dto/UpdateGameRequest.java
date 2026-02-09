package com.example.gameforum.game.dto;

import jakarta.validation.constraints.Size;

public record UpdateGameRequest(
        @Size(min = 2, max = 120) String title,
        @Size(max = 5000) String description,
        String coverUrl
) {}
