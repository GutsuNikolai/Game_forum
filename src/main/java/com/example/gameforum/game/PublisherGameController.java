package com.example.gameforum.game;

import com.example.gameforum.game.dto.CreateGameRequest;
import com.example.gameforum.game.dto.UpdateGameRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
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

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug, Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new AccessDeniedException("Admin role is required");
        }

        gameService.deleteGame(slug);
        return ResponseEntity.noContent().build();
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
