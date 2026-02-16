package com.example.gameforum.profile.dto;

import java.util.List;

public record UpdateProfileRequest(
        String bio,
        String city,
        List<String> favoriteGames
) {
}
