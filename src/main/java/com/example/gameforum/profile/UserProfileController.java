package com.example.gameforum.profile;

import com.example.gameforum.profile.dto.UpdateAccountSettingsRequest;
import com.example.gameforum.profile.dto.UpdateProfileRequest;
import com.example.gameforum.profile.dto.UserProfileView;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public UserProfileView getProfile(Authentication authentication) {
        return profileService.getProfile(getUsername(authentication));
    }

    @PutMapping("/profile")
    public UserProfileView updateProfile(
            @RequestBody(required = false) UpdateProfileRequest request,
            Authentication authentication
    ) {
        return profileService.updateProfile(getUsername(authentication), request);
    }

    @PutMapping("/settings")
    public UserProfileView updateSettings(
            @RequestBody(required = false) UpdateAccountSettingsRequest request,
            Authentication authentication
    ) {
        return profileService.updateAccountSettings(getUsername(authentication), request);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileView uploadAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            Authentication authentication
    ) {
        return profileService.updateAvatar(getUsername(authentication), avatar);
    }

    private String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Требуется авторизация");
        }
        return authentication.getName();
    }
}
