package com.example.gameforum.game;

import com.example.gameforum.common.PageResponse;
import com.example.gameforum.game.dto.GameDetails;
import com.example.gameforum.game.dto.GameListItem;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<GameListItem> list(
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
//        var p = service.list(page, size);
//        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        return PageResponse.from(service.list(page, size));
    }

    @GetMapping("/{slug}")
    public GameDetails get(@PathVariable String slug) {
        return service.getBySlug(slug);
    }
}
