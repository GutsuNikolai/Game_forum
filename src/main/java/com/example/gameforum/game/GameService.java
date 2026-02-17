package com.example.gameforum.game;

import com.example.gameforum.common.NotFoundException;
import com.example.gameforum.game.dto.GameDetails;
import com.example.gameforum.game.dto.GameListItem;
import com.example.gameforum.review.ReviewService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.example.gameforum.game.dto.CreateGameRequest;
import com.example.gameforum.game.dto.UpdateGameRequest;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
@Service
public class GameService {

    private final GameRepository games;
    private final ReviewService reviews;

    public GameService(GameRepository games, ReviewService reviews) {
        this.games = games;
        this.reviews = reviews;
    }

    public Page<GameListItem> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return games.findAll(pageable).map(g ->
                new GameListItem(
                        g.getId(),
                        g.getSlug(),
                        g.getTitle(),
                        g.getDescription() == null ? "" : g.getDescription(),
                        g.getCoverUrl(),
                        g.getRatingAvg() == null ? 0.0 : g.getRatingAvg().doubleValue(),
                        g.getRatingCnt() == null ? 0 : g.getRatingCnt()
                )
        );
    }

    public GameDetails getBySlug(String slug) {
        GameEntity g = games.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        
        return new GameDetails(
                g.getSlug(),
                g.getTitle(),
                g.getDescription(),
                g.getCoverUrl(),
                g.getRatingAvg() == null ? 0.0 : g.getRatingAvg().doubleValue(),
                g.getRatingCnt() == null ? 0 : g.getRatingCnt(),
                reviews.getPublished(slug)
        );
    }

    @Transactional
    public void createGame(CreateGameRequest req) {
        String slug = req.slug().trim().toLowerCase();
        if (games.existsBySlug(slug)) throw new IllegalArgumentException("Slug already exists");

        OffsetDateTime now = OffsetDateTime.now();

        GameEntity g = GameEntity.builder()
                .slug(slug)
                .title(req.title().trim())
                .description(req.description() == null ? "" : req.description().trim())
                .coverUrl(req.coverUrl() == null ? "" : req.coverUrl().trim())
                .createdAt(now)
                .updatedAt(now)
                .ratingAvg(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .ratingCnt(0)
                .build();

        games.save(g);
    }

    @Transactional
    public void updateGame(String slug, UpdateGameRequest req) {
        GameEntity g = games.findBySlug(slug).orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (req.title() != null) g.setTitle(req.title().trim());
        if (req.description() != null) g.setDescription(req.description().trim());
        if (req.coverUrl() != null) g.setCoverUrl(req.coverUrl().trim());
        g.setUpdatedAt(OffsetDateTime.now());

        games.save(g);
    }
}
