package com.example.gameforum.rating;

import com.example.gameforum.game.GameEntity;
import com.example.gameforum.game.GameRepository;
import com.example.gameforum.rating.dto.RatingResult;
import com.example.gameforum.rating.dto.SetRatingRequest;
import com.example.gameforum.user.UserEntity;
import com.example.gameforum.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
public class RatingService {

    private final RatingRepository ratings;
    private final GameRepository games;
    private final UserRepository users;

    public RatingService(RatingRepository ratings, GameRepository games, UserRepository users) {
        this.ratings = ratings;
        this.games = games;
        this.users = users;
    }

    @Transactional
    public RatingResult setRating(String slug, String username, SetRatingRequest req) {
        GameEntity game = games.findBySlug(slug).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        UserEntity user = users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        short value = (short) req.value();
        OffsetDateTime now = OffsetDateTime.now();

        RatingEntity r = ratings.findByGame_SlugAndUser_Username(slug, username).orElse(null);
        if (r == null) {
            r = RatingEntity.builder()
                    .game(game)
                    .user(user)
                    .value(value)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        } else {
            r.setValue(value);
            r.setUpdatedAt(now);
        }
        ratings.save(r);

        // пересчет агрегата
        Object[] row = ratings.getAvgAndCount(game.getId()).get(0);

        Number avgNum = (Number) row[0];
        Number cntNum = (Number) row[1];

        double avg = avgNum.doubleValue();
        int cnt = cntNum.intValue();

        // округлим до 2 знаков, как в БД numeric(3,2)
        BigDecimal rounded = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
        game.setRatingAvg(rounded);
        game.setRatingCnt(cnt);
        games.save(game);

        return new RatingResult(rounded.doubleValue(), cnt);
    }
}
