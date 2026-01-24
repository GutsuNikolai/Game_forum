package com.example.gameforum.comment;

import com.example.gameforum.comment.dto.CommentView;
import com.example.gameforum.comment.dto.CreateCommentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games/{slug}/comments")
public class CommentController {

    private final CommentService service;

    @GetMapping
    public Page<CommentView> list(@PathVariable String slug,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return service.list(slug, page, size);
    }

    @PostMapping
    public CommentView create(@PathVariable String slug,
                              @Valid @RequestBody CreateCommentRequest req,
                              Authentication auth) {
        if (auth == null) throw new IllegalArgumentException("Unauthorized");
        return service.create(slug, auth.getName(), req);
    }
}
