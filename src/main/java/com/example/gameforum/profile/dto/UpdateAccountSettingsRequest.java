package com.example.gameforum.profile.dto;

public record UpdateAccountSettingsRequest(
        String email,
        String currentPassword,
        String newPassword,
        Boolean emailNotifications,
        Boolean publicProfile
) {
}
