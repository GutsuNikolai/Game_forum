package com.example.gameforum.comment;

import com.example.gameforum.comment.dto.CommentView;
import com.example.gameforum.comment.dto.CreateCommentRequest;
import com.example.gameforum.game.GameEntity;
import com.example.gameforum.game.GameRepository;
import com.example.gameforum.user.UserEntity;
import com.example.gameforum.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository comments;
    private final GameRepository games;
    private final UserRepository users;

    public Page<CommentView> list(String slug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return comments.findByGame_SlugAndDeletedFalse(slug, pageable)
                .map(c -> new CommentView(
                        c.getId(),
                        c.getUser().getUsername(),
                        c.getContent(),
                        c.getCreatedAt()
                ));
    }

    public CommentView create(String slug, String username, CreateCommentRequest req) {
        GameEntity game = games.findBySlug(slug).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        UserEntity user = users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        CommentEntity c = CommentEntity.builder()
                .game(game)
                .user(user)
                .content(req.content().trim())
                .createdAt(OffsetDateTime.now())
                .deleted(false)
                .build();

        c = comments.save(c);

        return new CommentView(c.getId(), user.getUsername(), c.getContent(), c.getCreatedAt());
    }
}
