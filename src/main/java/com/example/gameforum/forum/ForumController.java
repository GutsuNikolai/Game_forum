package com.example.gameforum.forum;

import com.example.gameforum.forum.dto.CreateForumMessageRequest;
import com.example.gameforum.forum.dto.CreateForumTopicRequest;
import com.example.gameforum.forum.dto.ForumImageUploadView;
import com.example.gameforum.forum.dto.ForumMessageReactionView;
import com.example.gameforum.forum.dto.ForumMessageView;
import com.example.gameforum.forum.dto.ForumTopicView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumStorageService storageService;
    private final ForumUploadService uploadService;

    @GetMapping("/games/{gameId}/topics")
    public List<ForumTopicView> getGameTopics(@PathVariable Long gameId) {
        return storageService.getGameTopics(gameId);
    }

    @GetMapping("/topics/{topicId}")
    public ResponseEntity<ForumTopicView> getTopic(@PathVariable Long topicId) {
        return ResponseEntity.of(Optional.ofNullable(storageService.getTopic(topicId)));
    }

    @GetMapping("/latest-topics")
    public List<ForumTopicView> getLatestTopics(@RequestParam(defaultValue = "5") int limit) {
        return storageService.getLatestTopics(limit);
    }

    @PostMapping("/games/{gameId}/topics")
    public ResponseEntity<ForumTopicView> addTopic(
            @PathVariable Long gameId,
            @RequestBody CreateForumTopicRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("Требуется авторизация");
        }

        ForumTopicView created = storageService.addTopic(gameId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/topics/{topicId}/messages")
    public List<ForumMessageView> getTopicMessages(@PathVariable Long topicId) {
        return storageService.getTopicMessages(topicId);
    }

    @PostMapping("/topics/{topicId}/messages")
    public ResponseEntity<ForumMessageView> addMessage(
            @PathVariable Long topicId,
            @RequestBody CreateForumMessageRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("Требуется авторизация");
        }

        ForumMessageView created = storageService.addMessage(topicId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/messages/{messageId}/like")
    public ResponseEntity<ForumMessageReactionView> likeMessage(
            @PathVariable Long messageId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("Требуется авторизация");
        }

        ForumMessageReactionView updated = storageService.likeMessage(messageId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/messages/{messageId}/dislike")
    public ResponseEntity<ForumMessageReactionView> dislikeMessage(
            @PathVariable Long messageId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("Ð¢Ñ€ÐµÐ±ÑƒÐµÑ‚ÑÑ Ð°Ð²Ñ‚Ð¾Ñ€Ð¸Ð·Ð°Ñ†Ð¸Ñ");
        }

        ForumMessageReactionView updated = storageService.dislikeMessage(messageId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/uploads/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ForumImageUploadView> uploadImage(
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("Требуется авторизация");
        }

        String url = uploadService.storeImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ForumImageUploadView(url));
    }
}
