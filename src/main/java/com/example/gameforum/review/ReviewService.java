package com.example.gameforum.review;

import com.example.gameforum.game.GameEntity;
import com.example.gameforum.game.GameRepository;
import com.example.gameforum.review.dto.ReviewView;
import com.example.gameforum.review.dto.UpsertReviewRequest;
import com.example.gameforum.user.UserEntity;
import com.example.gameforum.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviews;
    private final GameRepository games;
    private final UserRepository users;

    // Для публичного просмотра: только PUBLISHED
    public ReviewView getPublished(String slug) {
        return reviews.findByGame_SlugAndStatus(slug, ReviewStatus.PUBLISHED)
                .map(this::toView)
                .orElse(null);
    }

    // Для publisher: видит и draft, и published
    public ReviewView getAny(String slug) {
        return reviews.findByGame_Slug(slug).map(this::toView).orElse(null);
    }

    @Transactional
    public ReviewView upsert(String slug, String publisherUsername, UpsertReviewRequest req) {
        GameEntity game = games.findBySlug(slug).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        UserEntity publisher = users.findByUsername(publisherUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        OffsetDateTime now = OffsetDateTime.now();

        ReviewEntity r = reviews.findByGame_Slug(slug).orElse(null);
        if (r == null) {
            r = ReviewEntity.builder()
                    .game(game)
                    .publisher(publisher)
                    .title(req.title().trim())
                    .content(req.content().trim())
                    .status(ReviewStatus.DRAFT)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        } else {
            r.setTitle(req.title().trim());
            r.setContent(req.content().trim());
            r.setUpdatedAt(now);
        }

        return toView(reviews.save(r));
    }

    @Transactional
    public ReviewView setStatus(String slug, ReviewStatus status) {
        ReviewEntity r = reviews.findByGame_Slug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        r.setStatus(status);
        r.setUpdatedAt(OffsetDateTime.now());
        return toView(reviews.save(r));
    }

    private ReviewView toView(ReviewEntity r) {
        return new ReviewView(
                r.getTitle(),
                r.getContent(),
                r.getStatus(),
                r.getPublisher().getUsername(),
                r.getUpdatedAt()
        );
    }
}
