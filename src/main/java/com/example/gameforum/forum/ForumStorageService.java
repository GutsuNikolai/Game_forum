package com.example.gameforum.forum;

import com.example.gameforum.forum.dto.CreateForumMessageRequest;
import com.example.gameforum.forum.dto.CreateForumTopicRequest;
import com.example.gameforum.forum.dto.ForumMessageView;
import com.example.gameforum.forum.dto.ForumTopicView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ForumStorageService {

    private static final String MESSAGES_SEED_RESOURCE = "data/forum-data.seed.json";
    private static final String TOPICS_SEED_RESOURCE = "data/forum-topics.seed.json";
    private static final List<String> AVATAR_COLORS = List.of(
            "#00b4db", "#0083b0", "#6bcf7f", "#ff6b6b",
            "#ffd93d", "#9c27b0", "#ff9800", "#3f51b5"
    );
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("ru-RU"));

    private final ObjectMapper objectMapper;
    private final Path messagesStoragePath = Path.of("data", "forum-data.json");
    private final Path topicsStoragePath = Path.of("data", "forum-topics.json");

    public ForumStorageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initializeMessagesStorage();
        initializeTopicsStorage();
    }

    public synchronized List<ForumTopicView> getGameTopics(Long gameId) {
        Map<String, List<ForumTopicView>> store = readTopicsStore();
        List<ForumTopicView> topics = new ArrayList<>(store.getOrDefault(gameId.toString(), List.of()));
        topics.sort(Comparator.comparing(ForumTopicView::id).reversed());
        return topics;
    }

    public synchronized ForumTopicView getTopic(Long topicId) {
        Map<String, List<ForumTopicView>> store = readTopicsStore();
        return store.values().stream()
                .flatMap(List::stream)
                .filter(topic -> Objects.equals(topic.id(), topicId))
                .findFirst()
                .orElse(null);
    }

    public synchronized ForumTopicView addTopic(Long gameId, String author, CreateForumTopicRequest request) {
        String title = sanitizeTopicTitle(request.title());
        String description = sanitizeTopicDescription(request.description());
        String icon = sanitizeTopicIcon(request.icon());

        Map<String, List<ForumTopicView>> store = readTopicsStore();
        long nextId = store.values().stream()
                .flatMap(List::stream)
                .map(ForumTopicView::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(100L) + 1;

        ForumTopicView created = new ForumTopicView(
                nextId,
                gameId,
                title,
                description,
                author,
                0,
                0,
                LocalDateTime.now().format(DATE_FORMATTER),
                icon
        );

        String key = gameId.toString();
        List<ForumTopicView> gameTopics = new ArrayList<>(store.getOrDefault(key, new ArrayList<>()));
        gameTopics.add(0, created);
        store.put(key, gameTopics);
        writeTopicsStore(store);

        return created;
    }

    public synchronized List<ForumMessageView> getTopicMessages(Long topicId) {
        Map<String, List<ForumMessageView>> store = readMessagesStore();
        return new ArrayList<>(store.getOrDefault(topicId.toString(), List.of()));
    }

    public synchronized ForumMessageView addMessage(Long topicId, String author, CreateForumMessageRequest request) {
        Map<String, List<ForumMessageView>> store = readMessagesStore();
        String key = topicId.toString();
        List<ForumMessageView> current = new ArrayList<>(store.getOrDefault(key, new ArrayList<>()));

        String sanitizedContent = sanitizeMessageContent(request.content());
        List<String> imageUrls = sanitizeImageUrls(request.imageUrls());

        if (sanitizedContent.isBlank() && imageUrls.isEmpty()) {
            throw new IllegalArgumentException("Введите текст сообщения или добавьте хотя бы одну ссылку на фото");
        }

        long nextId = current.stream()
                .map(ForumMessageView::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;

        ForumMessageView created = new ForumMessageView(
                nextId,
                author,
                pickAvatarColor(author),
                LocalDateTime.now().format(DATE_FORMATTER),
                sanitizedContent,
                imageUrls,
                0,
                0
        );

        current.add(created);
        store.put(key, current);
        writeMessagesStore(store);
        touchTopicOnMessage(topicId);

        return created;
    }

    private void touchTopicOnMessage(Long topicId) {
        Map<String, List<ForumTopicView>> store = readTopicsStore();
        boolean updated = false;
        String activityDate = LocalDateTime.now().format(DATE_FORMATTER);

        for (Map.Entry<String, List<ForumTopicView>> entry : store.entrySet()) {
            List<ForumTopicView> topics = new ArrayList<>(entry.getValue());
            for (int i = 0; i < topics.size(); i++) {
                ForumTopicView topic = topics.get(i);
                if (Objects.equals(topic.id(), topicId)) {
                    int nextReplies = Math.max(0, topic.replies() == null ? 0 : topic.replies()) + 1;
                    topics.set(i, new ForumTopicView(
                            topic.id(),
                            topic.gameId(),
                            topic.title(),
                            topic.description(),
                            topic.author(),
                            nextReplies,
                            topic.views() == null ? 0 : topic.views(),
                            activityDate,
                            topic.icon()
                    ));
                    store.put(entry.getKey(), topics);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                break;
            }
        }

        if (updated) {
            writeTopicsStore(store);
        }
    }

    private void initializeMessagesStorage() {
        initializeStorage(messagesStoragePath, MESSAGES_SEED_RESOURCE);
    }

    private void initializeTopicsStorage() {
        initializeStorage(topicsStoragePath, TOPICS_SEED_RESOURCE);
    }

    private void initializeStorage(Path storagePath, String seedResource) {
        try {
            Files.createDirectories(storagePath.getParent());
            if (Files.exists(storagePath)) {
                return;
            }

            ClassPathResource seed = new ClassPathResource(seedResource);
            if (seed.exists()) {
                try (InputStream input = seed.getInputStream()) {
                    Files.copy(input, storagePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), new LinkedHashMap<>());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось инициализировать JSON-хранилище форума", e);
        }
    }

    private Map<String, List<ForumMessageView>> readMessagesStore() {
        try {
            if (!Files.exists(messagesStoragePath)) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(messagesStoragePath.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать JSON-хранилище сообщений форума", e);
        }
    }

    private void writeMessagesStore(Map<String, List<ForumMessageView>> store) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(messagesStoragePath.toFile(), store);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить JSON-хранилище сообщений форума", e);
        }
    }

    private Map<String, List<ForumTopicView>> readTopicsStore() {
        try {
            if (!Files.exists(topicsStoragePath)) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(topicsStoragePath.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать JSON-хранилище тем форума", e);
        }
    }

    private void writeTopicsStore(Map<String, List<ForumTopicView>> store) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(topicsStoragePath.toFile(), store);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить JSON-хранилище тем форума", e);
        }
    }

    private String sanitizeMessageContent(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() > 5000) {
            throw new IllegalArgumentException("Слишком длинное сообщение (максимум 5000 символов)");
        }
        return trimmed;
    }

    private String sanitizeTopicTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Введите заголовок темы");
        }
        String normalized = title.trim();
        if (normalized.length() < 5 || normalized.length() > 120) {
            throw new IllegalArgumentException("Заголовок темы должен быть от 5 до 120 символов");
        }
        return normalized;
    }

    private String sanitizeTopicDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Введите описание темы");
        }

        String normalized = description.trim();
        if (normalized.length() < 10 || normalized.length() > 1000) {
            throw new IllegalArgumentException("Описание темы должно быть от 10 до 1000 символов");
        }
        return normalized;
    }

    private String sanitizeTopicIcon(String icon) {
        String normalized = icon == null ? "" : icon.trim();
        if (normalized.isBlank()) {
            return "fas fa-comments";
        }
        if (!normalized.matches("^fa[srb]?\\sfa-[a-z0-9-]+$")) {
            return "fas fa-comments";
        }
        return normalized;
    }

    private List<String> sanitizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }

        List<String> cleaned = imageUrls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(url -> !url.isBlank())
                .peek(url -> {
                    if (!url.matches("^https?://[^\\s\"'<>]+$")) {
                        throw new IllegalArgumentException("Ссылки на фото должны начинаться с http:// или https://");
                    }
                })
                .distinct()
                .toList();

        if (cleaned.size() > 5) {
            throw new IllegalArgumentException("Можно прикрепить максимум 5 ссылок на фото");
        }

        return cleaned;
    }

    private String pickAvatarColor(String author) {
        int index = Math.floorMod(author == null ? 0 : author.hashCode(), AVATAR_COLORS.size());
        return AVATAR_COLORS.get(index);
    }
}
