package com.example.gameforum.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null) {
            return Map.of("authenticated", false);
        }

        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())      // "ROLE_PUBLISHER"
                .findFirst()
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        return Map.of(
                "authenticated", true,
                "username", auth.getName(),
                "role", role
        );
    }
}
