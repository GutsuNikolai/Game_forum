package com.example.gameforum.game;

import com.example.gameforum.game.dto.GameDetails;
import com.example.gameforum.game.dto.GameListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService service;

    @GetMapping
    public Page<GameListItem> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @GetMapping("/{slug}")
    public GameDetails get(@PathVariable String slug) {
        return service.getBySlug(slug);
    }
}
