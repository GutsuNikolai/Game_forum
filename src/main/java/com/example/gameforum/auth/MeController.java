package com.example.gameforum.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null) return Map.of("authenticated", false);
        return Map.of(
                "authenticated", true,
                "username", auth.getName(),
                "authorities", auth.getAuthorities()
        );
    }
}
