package com.example.gameforum.review;

import com.example.gameforum.review.dto.ReviewView;
import com.example.gameforum.review.dto.UpsertReviewRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publisher/games/{slug}/review")
public class PublisherReviewController {

    private final ReviewService service;

    public PublisherReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public ReviewView get(@PathVariable String slug) {
        return service.getAny(slug);
    }

    @PutMapping
    public ReviewView upsert(@PathVariable String slug,
                             @Valid @RequestBody UpsertReviewRequest req,
                             Authentication auth) {
        return service.upsert(slug, auth.getName(), req);
    }

    @PatchMapping("/publish")
    public ReviewView publish(@PathVariable String slug) {
        return service.setStatus(slug, ReviewStatus.PUBLISHED);
    }

    @PatchMapping("/draft")
    public ReviewView draft(@PathVariable String slug) {
        return service.setStatus(slug, ReviewStatus.DRAFT);
    }
}
