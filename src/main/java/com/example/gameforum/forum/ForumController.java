package com.example.gameforum.forum;

import com.example.gameforum.forum.dto.CreateForumMessageRequest;
import com.example.gameforum.forum.dto.CreateForumTopicRequest;
import com.example.gameforum.forum.dto.ForumImageUploadView;
import com.example.gameforum.forum.dto.ForumMessageReactionView;
import com.example.gameforum.forum.dto.ForumMessageView;
import com.example.gameforum.forum.dto.ForumTopicView;
import com.example.gameforum.forum.dto.UpdateForumMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
            throw new IllegalArgumentException("Authentication required");
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
            throw new IllegalArgumentException("Authentication required");
        }

        ForumMessageView created = storageService.addMessage(topicId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<ForumMessageView> updateMessage(
            @PathVariable Long messageId,
            @RequestBody UpdateForumMessageRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication required");
        }

        ForumMessageView updated = storageService.updateMessage(
                messageId,
                authentication.getName(),
                isAdmin(authentication),
                request
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication
    ) {
        if (!isAdmin(authentication)) {
            throw new AccessDeniedException("Admin role is required");
        }

        storageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long topicId,
            Authentication authentication
    ) {
        if (!isAdmin(authentication)) {
            throw new AccessDeniedException("Admin role is required");
        }

        storageService.deleteTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages/{messageId}/like")
    public ResponseEntity<ForumMessageReactionView> likeMessage(
            @PathVariable Long messageId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication required");
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
            throw new IllegalArgumentException("Authentication required");
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
            throw new IllegalArgumentException("Authentication required");
        }

        String url = uploadService.storeImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ForumImageUploadView(url));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
