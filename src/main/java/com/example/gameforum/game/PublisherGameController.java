package com.example.gameforum.game;

import com.example.gameforum.game.dto.CreateGameRequest;
import com.example.gameforum.game.dto.UpdateGameRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/publisher/games")
public class PublisherGameController {

    private final GameService gameService;

    public PublisherGameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public void create(@Valid @RequestBody CreateGameRequest req) {
        gameService.createGame(req);
    }

    @PatchMapping("/{slug}")
    public void update(@PathVariable String slug, @Valid @RequestBody UpdateGameRequest req) {
        gameService.updateGame(slug, req);
    }
}
