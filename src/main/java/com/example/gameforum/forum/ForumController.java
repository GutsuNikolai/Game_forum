package com.example.gameforum.forum;

import com.example.gameforum.forum.dto.CreateForumMessageRequest;
import com.example.gameforum.forum.dto.CreateForumTopicRequest;
import com.example.gameforum.forum.dto.ForumMessageView;
import com.example.gameforum.forum.dto.ForumTopicView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumStorageService storageService;

    @GetMapping("/games/{gameId}/topics")
    public List<ForumTopicView> getGameTopics(@PathVariable Long gameId) {
        return storageService.getGameTopics(gameId);
    }

    @GetMapping("/topics/{topicId}")
    public ResponseEntity<ForumTopicView> getTopic(@PathVariable Long topicId) {
        return ResponseEntity.of(Optional.ofNullable(storageService.getTopic(topicId)));
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
}
