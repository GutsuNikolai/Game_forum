package com.example.gameforum.game;

import com.example.gameforum.game.dto.GameDetails;
import com.example.gameforum.game.dto.GameListItem;
import com.example.gameforum.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository games;
    private final ReviewService reviews;

    public Page<GameListItem> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return games.findAll(pageable).map(g ->
                new GameListItem(
                        g.getSlug(),
                        g.getTitle(),
                        g.getCoverUrl(),
                        g.getRatingAvg() == null ? 0.0 : g.getRatingAvg().doubleValue(),
                        g.getRatingCnt() == null ? 0 : g.getRatingCnt()
                )
        );
    }

    public GameDetails getBySlug(String slug) {
        GameEntity g = games.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
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
}
