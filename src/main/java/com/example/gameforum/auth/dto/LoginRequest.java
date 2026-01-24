package com.example.gameforum.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String login,     // username ИЛИ email
        @NotBlank String password
) {}
