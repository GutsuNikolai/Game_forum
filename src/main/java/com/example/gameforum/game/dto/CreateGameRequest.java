package com.example.gameforum.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGameRequest(
        @NotBlank @Size(min = 2, max = 80) String slug,
        @NotBlank @Size(min = 2, max = 120) String title,
        @Size(max = 5000) String description,
        String coverUrl
) {}
