package com.example.gameforum.rating;

import com.example.gameforum.rating.dto.RatingResult;
import com.example.gameforum.rating.dto.SetRatingRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games/{slug}/rating")
public class RatingController {

    private final RatingService service;

    public RatingController(RatingService service) {
        this.service = service;
    }

    @PostMapping
    public RatingResult set(@PathVariable String slug,
                            @Valid @RequestBody SetRatingRequest req,
                            Authentication auth) {
        if (auth == null) throw new IllegalArgumentException("Unauthorized");
        return service.setRating(slug, auth.getName(), req);
    }
}
