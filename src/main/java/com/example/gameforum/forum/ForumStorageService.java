package com.example.gameforum.forum;

import com.example.gameforum.forum.dto.CreateForumMessageRequest;
import com.example.gameforum.forum.dto.CreateForumTopicRequest;
import com.example.gameforum.forum.dto.ForumMessageView;
import com.example.gameforum.forum.dto.ForumTopicView;
import com.example.gameforum.game.GameRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ForumStorageService {

    private static final List<String> AVATAR_COLORS = List.of(
            "#00b4db", "#0083b0", "#6bcf7f", "#ff6b6b",
            "#ffd93d", "#9c27b0", "#ff9800", "#3f51b5"
    );
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("ru-RU"));
    private static final String DEFAULT_TOPIC_ICON = "fas fa-comments";

    private final ForumTopicRepository topics;
    private final ForumMessageRepository messages;
    private final GameRepository games;

    public ForumStorageService(ForumTopicRepository topics, ForumMessageRepository messages, GameRepository games) {
        this.topics = topics;
        this.messages = messages;
        this.games = games;
    }

    public List<ForumTopicView> getGameTopics(Long gameId) {
        return topics.findByGameIdOrderByIdDesc(gameId).stream()
                .map(this::toTopicView)
                .toList();
    }

    public ForumTopicView getTopic(Long topicId) {
        return topics.findById(topicId)
                .map(this::toTopicView)
                .orElse(null);
    }

    public List<ForumTopicView> getLatestTopics(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return topics.findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "lastActivityAt")))
                .stream()
                .map(this::toTopicView)
                .toList();
    }

    @Transactional
    public ForumTopicView addTopic(Long gameId, String author, CreateForumTopicRequest request) {
        if (!games.existsById(gameId)) {
            throw new IllegalArgumentException("Игра не найдена");
        }

        OffsetDateTime now = OffsetDateTime.now();
        ForumTopicEntity topic = ForumTopicEntity.builder()
                .gameId(gameId)
                .title(sanitizeTopicTitle(request.title()))
                .description(sanitizeTopicDescription(request.description()))
                .author(author)
                .replies(0)
                .views(0)
                .icon(sanitizeTopicIcon(request.icon()))
                .createdAt(now)
                .lastActivityAt(now)
                .build();

        return toTopicView(topics.save(topic));
    }

    public List<ForumMessageView> getTopicMessages(Long topicId) {
        return messages.findByTopicIdOrderByIdAsc(topicId).stream()
                .map(this::toMessageView)
                .toList();
    }

    @Transactional
    public ForumMessageView addMessage(Long topicId, String author, CreateForumMessageRequest request) {
        ForumTopicEntity topic = topics.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Тема не найдена"));

        String sanitizedContent = sanitizeMessageContent(request.content());
        List<String> imageUrls = sanitizeImageUrls(request.imageUrls());
        if (sanitizedContent.isBlank() && imageUrls.isEmpty()) {
            throw new IllegalArgumentException("Введите текст сообщения или добавьте хотя бы одну ссылку на фото");
        }

        OffsetDateTime now = OffsetDateTime.now();
        ForumMessageEntity message = ForumMessageEntity.builder()
                .topicId(topicId)
                .author(author)
                .avatarColor(pickAvatarColor(author))
                .content(sanitizedContent)
                .imageUrlsText(joinImageUrls(imageUrls))
                .likes(0)
                .replies(0)
                .createdAt(now)
                .build();

        ForumMessageEntity saved = messages.save(message);

        topic.setReplies((topic.getReplies() == null ? 0 : topic.getReplies()) + 1);
        topic.setLastActivityAt(now);
        topics.save(topic);

        return toMessageView(saved);
    }

    private ForumTopicView toTopicView(ForumTopicEntity topic) {
        return new ForumTopicView(
                topic.getId(),
                topic.getGameId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getAuthor(),
                topic.getReplies() == null ? 0 : topic.getReplies(),
                topic.getViews() == null ? 0 : topic.getViews(),
                formatDate(topic.getLastActivityAt()),
                sanitizeTopicIcon(topic.getIcon())
        );
    }

    private ForumMessageView toMessageView(ForumMessageEntity message) {
        return new ForumMessageView(
                message.getId(),
                message.getAuthor(),
                message.getAvatarColor(),
                formatDate(message.getCreatedAt()),
                message.getContent(),
                splitImageUrls(message.getImageUrlsText()),
                message.getLikes() == null ? 0 : message.getLikes(),
                message.getReplies() == null ? 0 : message.getReplies()
        );
    }

    private String formatDate(OffsetDateTime value) {
        OffsetDateTime safe = value == null ? OffsetDateTime.now() : value;
        return safe.format(DATE_FORMATTER);
    }

    private String joinImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return "";
        }
        return String.join("\n", imageUrls);
    }

    private List<String> splitImageUrls(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        return Arrays.stream(raw.split("\n"))
                .map(String::trim)
                .filter(url -> !url.isBlank())
                .toList();
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
            return DEFAULT_TOPIC_ICON;
        }
        if (!normalized.matches("^fa[srb]?\\sfa-[a-z0-9-]+$")) {
            return DEFAULT_TOPIC_ICON;
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
