package com.example.gameforum.forum;

import com.example.gameforum.forum.dto.CreateForumMessageRequest;
import com.example.gameforum.forum.dto.CreateForumTopicRequest;
import com.example.gameforum.forum.dto.ForumMessageReactionView;
import com.example.gameforum.forum.dto.ForumMessageView;
import com.example.gameforum.forum.dto.ForumTopicView;
import com.example.gameforum.forum.dto.UpdateForumMessageRequest;
import com.example.gameforum.game.GameRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ForumStorageService {

    private static final List<String> AVATAR_COLORS = List.of(
            "#00b4db", "#0083b0", "#6bcf7f", "#ff6b6b",
            "#ffd93d", "#9c27b0", "#ff9800", "#3f51b5"
    );
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("ru-RU"));
    private static final String DEFAULT_TOPIC_ICON = "fas fa-comments";
    private static final int MAX_MESSAGE_LENGTH = 5000;
    private static final int MAX_TOPIC_TITLE = 120;
    private static final int MAX_TOPIC_DESCRIPTION = 1000;
    private static final int MAX_IMAGE_URLS = 5;
    private static final int MAX_QUOTE_PREVIEW = 180;

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
            throw new IllegalArgumentException("Game not found");
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
        List<ForumMessageEntity> inTopic = messages.findByTopicIdOrderByIdAsc(topicId);
        Map<Long, ForumMessageEntity> byId = inTopic.stream()
                .collect(Collectors.toMap(ForumMessageEntity::getId, m -> m));

        return inTopic.stream()
                .map(message -> toMessageView(message, byId))
                .toList();
    }

    @Transactional
    public ForumMessageView addMessage(Long topicId, String author, CreateForumMessageRequest request) {
        ForumTopicEntity topic = topics.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        String sanitizedContent = sanitizeMessageContent(request.content());
        List<String> imageUrls = sanitizeImageUrls(request.imageUrls());
        if (sanitizedContent.isBlank() && imageUrls.isEmpty()) {
            throw new IllegalArgumentException("Message must contain text or at least one image");
        }

        Long parentMessageId = normalizeMessageReference(request.parentMessageId(), topicId);
        Long quotedMessageId = normalizeMessageReference(request.quotedMessageId(), topicId);
        if (parentMessageId == null && quotedMessageId != null) {
            parentMessageId = quotedMessageId;
        }

        OffsetDateTime now = OffsetDateTime.now();
        ForumMessageEntity message = ForumMessageEntity.builder()
                .topicId(topicId)
                .author(author)
                .avatarColor(pickAvatarColor(author))
                .content(sanitizedContent)
                .imageUrlsText(joinImageUrls(imageUrls))
                .parentMessageId(parentMessageId)
                .quotedMessageId(quotedMessageId)
                .likes(0)
                .dislikes(0)
                .replies(0)
                .createdAt(now)
                .editedAt(null)
                .build();

        ForumMessageEntity saved = messages.save(message);

        if (parentMessageId != null) {
            messages.findById(parentMessageId).ifPresent(parent -> {
                parent.setReplies((parent.getReplies() == null ? 0 : parent.getReplies()) + 1);
                messages.save(parent);
            });
        }

        topic.setReplies((topic.getReplies() == null ? 0 : topic.getReplies()) + 1);
        topic.setLastActivityAt(now);
        topics.save(topic);

        return toMessageView(saved, Map.of());
    }

    @Transactional
    public ForumMessageView updateMessage(Long messageId, String actorUsername, boolean isAdmin, UpdateForumMessageRequest request) {
        ForumMessageEntity message = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!isAdmin && !Objects.equals(message.getAuthor(), actorUsername)) {
            throw new AccessDeniedException("You can edit only your own messages");
        }

        String sanitizedContent = sanitizeMessageContent(request.content());
        List<String> imageUrls = sanitizeImageUrls(request.imageUrls());
        if (sanitizedContent.isBlank() && imageUrls.isEmpty()) {
            throw new IllegalArgumentException("Message must contain text or at least one image");
        }

        OffsetDateTime now = OffsetDateTime.now();
        message.setContent(sanitizedContent);
        message.setImageUrlsText(joinImageUrls(imageUrls));
        message.setEditedAt(now);
        ForumMessageEntity saved = messages.save(message);

        touchTopicLastActivity(saved.getTopicId(), now);
        return toMessageView(saved, Map.of());
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        ForumMessageEntity message = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        Long topicId = message.getTopicId();
        Long parentMessageId = message.getParentMessageId();

        messages.delete(message);

        if (parentMessageId != null) {
            messages.findById(parentMessageId).ifPresent(parent -> {
                int currentReplies = parent.getReplies() == null ? 0 : parent.getReplies();
                parent.setReplies(Math.max(0, currentReplies - 1));
                messages.save(parent);
            });
        }

        OffsetDateTime now = OffsetDateTime.now();
        topics.findById(topicId).ifPresent(topic -> {
            topic.setReplies((int) messages.countByTopicId(topicId));
            topic.setLastActivityAt(now);
            topics.save(topic);
        });
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        if (!topics.existsById(topicId)) {
            throw new IllegalArgumentException("Topic not found");
        }
        topics.deleteById(topicId);
    }

    @Transactional
    public ForumMessageReactionView likeMessage(Long messageId) {
        ForumMessageEntity message = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

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
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

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

    private ForumMessageView toMessageView(ForumMessageEntity message, Map<Long, ForumMessageEntity> inTopicById) {
        ForumMessageEntity quoted = null;
        Long quotedMessageId = message.getQuotedMessageId();
        if (quotedMessageId != null) {
            quoted = inTopicById.get(quotedMessageId);
            if (quoted == null) {
                quoted = messages.findById(quotedMessageId).orElse(null);
            }
        }

        String quotedAuthor = quoted == null ? null : quoted.getAuthor();
        String quotedPreview = quoted == null ? null : abbreviate(stripHtml(quoted.getContent()), MAX_QUOTE_PREVIEW);

        return new ForumMessageView(
                message.getId(),
                message.getAuthor(),
                message.getAvatarColor(),
                formatDate(message.getCreatedAt()),
                message.getContent(),
                splitImageUrls(message.getImageUrlsText()),
                message.getParentMessageId(),
                quotedMessageId,
                quotedAuthor,
                quotedPreview,
                message.getLikes() == null ? 0 : message.getLikes(),
                message.getDislikes() == null ? 0 : message.getDislikes(),
                message.getReplies() == null ? 0 : message.getReplies(),
                message.getEditedAt() != null
        );
    }

    private Long normalizeMessageReference(Long messageId, Long topicId) {
        if (messageId == null) {
            return null;
        }

        ForumMessageEntity referenced = messages.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Referenced message not found"));

        if (!Objects.equals(referenced.getTopicId(), topicId)) {
            throw new IllegalArgumentException("Referenced message belongs to another topic");
        }

        return referenced.getId();
    }

    private void touchTopicLastActivity(Long topicId, OffsetDateTime at) {
        topics.findById(topicId).ifPresent(topic -> {
            topic.setLastActivityAt(at);
            topics.save(topic);
        });
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
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message is too long");
        }
        return trimmed;
    }

    private String sanitizeTopicTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Topic title is required");
        }
        String normalized = title.trim();
        if (normalized.length() < 5 || normalized.length() > MAX_TOPIC_TITLE) {
            throw new IllegalArgumentException("Topic title must contain 5..120 characters");
        }
        return normalized;
    }

    private String sanitizeTopicDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Topic description is required");
        }
        String normalized = description.trim();
        if (normalized.length() < 10 || normalized.length() > MAX_TOPIC_DESCRIPTION) {
            throw new IllegalArgumentException("Topic description must contain 10..1000 characters");
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
                        throw new IllegalArgumentException("Image URL must be http(s) or /uploads/...");
                    }
                })
                .distinct()
                .toList();

        if (cleaned.size() > MAX_IMAGE_URLS) {
            throw new IllegalArgumentException("Maximum 5 images per message");
        }

        return cleaned;
    }

    private String pickAvatarColor(String author) {
        int index = Math.floorMod(author == null ? 0 : author.hashCode(), AVATAR_COLORS.size());
        return AVATAR_COLORS.get(index);
    }

    private String stripHtml(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= 3) {
            return value.substring(0, Math.max(0, maxLength));
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
