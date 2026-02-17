package com.example.gameforum.profile;

import com.example.gameforum.profile.dto.UpdateAccountSettingsRequest;
import com.example.gameforum.profile.dto.UpdateProfileRequest;
import com.example.gameforum.profile.dto.UserProfileView;
import com.example.gameforum.forum.ForumMessageRepository;
import com.example.gameforum.forum.ForumTopicRepository;
import com.example.gameforum.user.UserEntity;
import com.example.gameforum.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserProfileService {

    private static final String SEED_RESOURCE = "data/user-profile-data.seed.json";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final long MAX_AVATAR_BYTES = 5L * 1024L * 1024L;
    private static final Path AVATARS_PATH = Path.of("data", "uploads", "profile")
            .toAbsolutePath()
            .normalize();
    private static final DateTimeFormatter MEMBER_SINCE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("ru-RU"));

    private final UserRepository users;
    private final ForumTopicRepository forumTopics;
    private final ForumMessageRepository forumMessages;
    private final PasswordEncoder encoder;
    private final ObjectMapper objectMapper;
    private final Path storagePath = Path.of("data", "user-profile-data.json");

    public UserProfileService(
            UserRepository users,
            ForumTopicRepository forumTopics,
            ForumMessageRepository forumMessages,
            PasswordEncoder encoder,
            ObjectMapper objectMapper
    ) {
        this.users = users;
        this.forumTopics = forumTopics;
        this.forumMessages = forumMessages;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
        initializeStorage();
    }

    public synchronized UserProfileView getProfile(String username) {
        UserEntity user = getRequiredUser(username);
        Map<String, StoredUserProfile> store = readStore();
        StoredUserProfile profile = store.getOrDefault(user.getUsername(), StoredUserProfile.defaults());
        return toView(user, profile);
    }

    public synchronized UserProfileView updateProfile(String username, UpdateProfileRequest request) {
        UserEntity user = getRequiredUser(username);
        Map<String, StoredUserProfile> store = readStore();
        StoredUserProfile profile = store.getOrDefault(user.getUsername(), StoredUserProfile.defaults());

        String bio = sanitizeBio(request == null ? null : request.bio());
        String city = sanitizeCity(request == null ? null : request.city());
        List<String> favoriteGames = sanitizeFavoriteGames(request == null ? null : request.favoriteGames());

        profile.bio = bio;
        profile.city = city;
        profile.favoriteGames = favoriteGames;
        profile.updatedAt = OffsetDateTime.now().toString();

        store.put(user.getUsername(), profile);
        writeStore(store);

        return toView(user, profile);
    }

    public synchronized UserProfileView updateAccountSettings(String username, UpdateAccountSettingsRequest request) {
        UserEntity user = getRequiredUser(username);
        Map<String, StoredUserProfile> store = readStore();
        StoredUserProfile profile = store.getOrDefault(user.getUsername(), StoredUserProfile.defaults());

        if (request != null) {
            updateEmailIfNeeded(user, request.email());
            updatePasswordIfNeeded(user, request.currentPassword(), request.newPassword());

            if (request.emailNotifications() != null) {
                profile.emailNotifications = request.emailNotifications();
            }

            if (request.publicProfile() != null) {
                profile.publicProfile = request.publicProfile();
            }
        }

        profile.updatedAt = OffsetDateTime.now().toString();
        users.save(user);
        store.put(user.getUsername(), profile);
        writeStore(store);

        return toView(user, profile);
    }

    public synchronized UserProfileView updateAvatar(String username, MultipartFile avatarFile) {
        UserEntity user = getRequiredUser(username);
        Map<String, StoredUserProfile> store = readStore();
        StoredUserProfile profile = store.getOrDefault(user.getUsername(), StoredUserProfile.defaults());

        profile.avatarUrl = storeAvatar(avatarFile);
        profile.updatedAt = OffsetDateTime.now().toString();

        store.put(user.getUsername(), profile);
        writeStore(store);

        return toView(user, profile);
    }

    private void updateEmailIfNeeded(UserEntity user, String emailRaw) {
        if (emailRaw == null) {
            return;
        }

        String email = emailRaw.trim().toLowerCase(Locale.ROOT);
        if (email.isBlank()) {
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Введите корректный email");
        }

        if (email.equals(user.getEmail())) {
            return;
        }

        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Этот email уже используется");
        }

        user.setEmail(email);
    }

    private void updatePasswordIfNeeded(UserEntity user, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Введите текущий пароль");
        }

        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Текущий пароль введён неверно");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Новый пароль должен быть минимум 8 символов");
        }

        user.setPasswordHash(encoder.encode(newPassword));
    }

    private UserProfileView toView(UserEntity user, StoredUserProfile profile) {
        String memberSince = user.getCreatedAt() == null
                ? "-"
                : user.getCreatedAt().format(MEMBER_SINCE_FORMATTER);
        long topicsCreated = forumTopics.countByAuthor(user.getUsername());
        long commentsCreated = forumMessages.countByAuthor(user.getUsername());

        return new UserProfileView(
                user.getUsername(),
                user.getEmail(),
                user.getRole() == null ? "USER" : user.getRole().name(),
                Optional.ofNullable(profile.avatarUrl).orElse(""),
                Optional.ofNullable(profile.bio).orElse(""),
                Optional.ofNullable(profile.city).orElse("Не указан"),
                List.copyOf(Optional.ofNullable(profile.favoriteGames).orElse(List.of())),
                profile.emailNotifications == null || profile.emailNotifications,
                profile.publicProfile == null || profile.publicProfile,
                memberSince,
                topicsCreated,
                commentsCreated
        );
    }

    private UserEntity getRequiredUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    private String sanitizeBio(String bioRaw) {
        String bio = bioRaw == null ? "" : bioRaw.trim();
        if (bio.length() > 1200) {
            throw new IllegalArgumentException("Описание профиля не должно превышать 1200 символов");
        }
        return bio;
    }

    private String sanitizeCity(String cityRaw) {
        String city = cityRaw == null ? "" : cityRaw.trim();
        if (city.length() > 80) {
            throw new IllegalArgumentException("Название города не должно превышать 80 символов");
        }
        return city;
    }

    private List<String> sanitizeFavoriteGames(List<String> favoriteGamesRaw) {
        if (favoriteGamesRaw == null) {
            return List.of();
        }

        List<String> games = favoriteGamesRaw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.length() > 60 ? value.substring(0, 60) : value)
                .distinct()
                .toList();

        if (games.size() > 12) {
            throw new IllegalArgumentException("Можно указать максимум 12 любимых игр");
        }

        return games;
    }

    private String storeAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("Файл слишком большой (максимум 5 MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Разрешены только изображения");
        }

        String extension = resolveImageExtension(file.getOriginalFilename(), contentType);
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            Files.createDirectories(AVATARS_PATH);
            Path target = AVATARS_PATH.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить аватар", e);
        }

        return "/uploads/profile/" + fileName;
    }

    private String resolveImageExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                String ext = originalFilename.substring(dot).toLowerCase(Locale.ROOT);
                if (ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
                    return ext;
                }
            }
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".jpg";
        };
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(storagePath.getParent());
            if (Files.exists(storagePath)) {
                return;
            }

            ClassPathResource seed = new ClassPathResource(SEED_RESOURCE);
            if (seed.exists()) {
                try (InputStream input = seed.getInputStream()) {
                    Files.copy(input, storagePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                writeStore(new LinkedHashMap<>());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось инициализировать JSON-хранилище профилей", e);
        }
    }

    private Map<String, StoredUserProfile> readStore() {
        try {
            if (!Files.exists(storagePath)) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(storagePath.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать JSON-хранилище профилей", e);
        }
    }

    private void writeStore(Map<String, StoredUserProfile> store) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), store);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить JSON-хранилище профилей", e);
        }
    }

    public static class StoredUserProfile {
        public String avatarUrl;
        public String bio;
        public String city;
        public List<String> favoriteGames;
        public Boolean emailNotifications;
        public Boolean publicProfile;
        public String updatedAt;

        public static StoredUserProfile defaults() {
            StoredUserProfile profile = new StoredUserProfile();
            profile.avatarUrl = "";
            profile.bio = "";
            profile.city = "Не указан";
            profile.favoriteGames = new ArrayList<>();
            profile.emailNotifications = true;
            profile.publicProfile = true;
            profile.updatedAt = OffsetDateTime.now().toString();
            return profile;
        }
    }
}
