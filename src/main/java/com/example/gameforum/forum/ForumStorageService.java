package com.example.gameforum.forum;

import com.example.gameforum.forum.dto.CreateForumMessageRequest;
import com.example.gameforum.forum.dto.CreateForumTopicRequest;
import com.example.gameforum.forum.dto.ForumMessageReactionView;
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
            throw new IllegalArgumentException("Ð˜Ð³Ñ€Ð° Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°");
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
                .orElseThrow(() -> new IllegalArgumentException("Ð¢ÐµÐ¼Ð° Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°"));

        String sanitizedContent = sanitizeMessageContent(request.content());
        List<String> imageUrls = sanitizeImageUrls(request.imageUrls());
        if (sanitizedContent.isBlank() && imageUrls.isEmpty()) {
            throw new IllegalArgumentException("Ð’Ð²ÐµÐ´Ð¸Ñ‚Ðµ Ñ‚ÐµÐºÑÑ‚ ÑÐ¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ñ Ð¸Ð»Ð¸ Ð´Ð¾Ð±Ð°Ð²ÑŒÑ‚Ðµ Ñ…Ð¾Ñ‚Ñ Ð±Ñ‹ Ð¾Ð´Ð½Ñƒ ÑÑÑ‹Ð»ÐºÑƒ Ð½Ð° Ñ„Ð¾Ñ‚Ð¾");
        }

        OffsetDateTime now = OffsetDateTime.now();
        ForumMessageEntity message = ForumMessageEntity.builder()
                .topicId(topicId)
                .author(author)
                .avatarColor(pickAvatarColor(author))
                .content(sanitizedContent)
                .imageUrlsText(joinImageUrls(imageUrls))
                .likes(0)
                .dislikes(0)
                .replies(0)
                .createdAt(now)
                .build();

        ForumMessageEntity saved = messages.save(message);

        topic.setReplies((topic.getReplies() == null ? 0 : topic.getReplies()) + 1);
        topic.setLastActivityAt(now);
        topics.save(topic);

        return toMessageView(saved);
    }

    @Transactional
    public ForumMessageReactionView likeMessage(Long messageId) {
        ForumMessageEntity message = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Ð¡Ð¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ðµ Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð¾"));

        int updatedLikes = (message.getLikes() == null ? 0 : message.getLikes()) + 1;
        message.setLikes(updatedLikes);
        messages.save(message);

        return new ForumMessageReactionView(
                message.getId(),
                updatedLikes,
                message.getDislikes() == null ? 0 : message.getDislikes()
        );
    }

    @Transactional
    public ForumMessageReactionView dislikeMessage(Long messageId) {
        ForumMessageEntity message = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("ÃÂ¡ÃÂ¾ÃÂ¾ÃÂ±Ã‘â€°ÃÂµÃÂ½ÃÂ¸ÃÂµ ÃÂ½ÃÂµ ÃÂ½ÃÂ°ÃÂ¹ÃÂ´ÃÂµÃÂ½ÃÂ¾"));

        int updatedDislikes = (message.getDislikes() == null ? 0 : message.getDislikes()) + 1;
        message.setDislikes(updatedDislikes);
        messages.save(message);

        return new ForumMessageReactionView(
                message.getId(),
                message.getLikes() == null ? 0 : message.getLikes(),
                updatedDislikes
        );
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
                message.getDislikes() == null ? 0 : message.getDislikes(),
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
            throw new IllegalArgumentException("Ð¡Ð»Ð¸ÑˆÐºÐ¾Ð¼ Ð´Ð»Ð¸Ð½Ð½Ð¾Ðµ ÑÐ¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ðµ (Ð¼Ð°ÐºÑÐ¸Ð¼ÑƒÐ¼ 5000 ÑÐ¸Ð¼Ð²Ð¾Ð»Ð¾Ð²)");
        }
        return trimmed;
    }

    private String sanitizeTopicTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Ð’Ð²ÐµÐ´Ð¸Ñ‚Ðµ Ð·Ð°Ð³Ð¾Ð»Ð¾Ð²Ð¾Ðº Ñ‚ÐµÐ¼Ñ‹");
        }
        String normalized = title.trim();
        if (normalized.length() < 5 || normalized.length() > 120) {
            throw new IllegalArgumentException("Ð—Ð°Ð³Ð¾Ð»Ð¾Ð²Ð¾Ðº Ñ‚ÐµÐ¼Ñ‹ Ð´Ð¾Ð»Ð¶ÐµÐ½ Ð±Ñ‹Ñ‚ÑŒ Ð¾Ñ‚ 5 Ð´Ð¾ 120 ÑÐ¸Ð¼Ð²Ð¾Ð»Ð¾Ð²");
        }
        return normalized;
    }

    private String sanitizeTopicDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Ð’Ð²ÐµÐ´Ð¸Ñ‚Ðµ Ð¾Ð¿Ð¸ÑÐ°Ð½Ð¸Ðµ Ñ‚ÐµÐ¼Ñ‹");
        }
        String normalized = description.trim();
        if (normalized.length() < 10 || normalized.length() > 1000) {
            throw new IllegalArgumentException("ÐžÐ¿Ð¸ÑÐ°Ð½Ð¸Ðµ Ñ‚ÐµÐ¼Ñ‹ Ð´Ð¾Ð»Ð¶Ð½Ð¾ Ð±Ñ‹Ñ‚ÑŒ Ð¾Ñ‚ 10 Ð´Ð¾ 1000 ÑÐ¸Ð¼Ð²Ð¾Ð»Ð¾Ð²");
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
                    boolean isRemote = url.matches("^https?://[^\\s\"'<>]+$");
                    boolean isLocalUpload = url.matches("^/uploads/[a-zA-Z0-9._\\-/]+$");
                    if (!isRemote && !isLocalUpload) {
                        throw new IllegalArgumentException("Image URLs must be http(s) or /uploads/...");
                    }
                })
                .distinct()
                .toList();

        if (cleaned.size() > 5) {
            throw new IllegalArgumentException("ÐœÐ¾Ð¶Ð½Ð¾ Ð¿Ñ€Ð¸ÐºÑ€ÐµÐ¿Ð¸Ñ‚ÑŒ Ð¼Ð°ÐºÑÐ¸Ð¼ÑƒÐ¼ 5 ÑÑÑ‹Ð»Ð¾Ðº Ð½Ð° Ñ„Ð¾Ñ‚Ð¾");
        }

        return cleaned;
    }

    private String pickAvatarColor(String author) {
        int index = Math.floorMod(author == null ? 0 : author.hashCode(), AVATAR_COLORS.size());
        return AVATAR_COLORS.get(index);
    }
}

