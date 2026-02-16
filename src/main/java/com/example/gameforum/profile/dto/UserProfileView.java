package com.example.gameforum.profile.dto;

import java.util.List;

public record UserProfileView(
        String username,
        String email,
        String role,
        String bio,
        String city,
        List<String> favoriteGames,
        boolean emailNotifications,
        boolean publicProfile,
        String memberSince
) {
}
