package com.example.gameforum.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SetRatingRequest(
        @Min(1) @Max(10) int value
) {}
