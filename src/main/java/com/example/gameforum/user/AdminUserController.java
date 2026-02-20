package com.example.gameforum.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository users;

    public AdminUserController(UserRepository users) {
        this.users = users;
    }

    @PutMapping("/{username}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable String username,
            @RequestBody UpdateUserRoleRequest request
    ) {
        UserEntity user = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserRole role = parseRole(request.role());
        user.setRole(role);
        users.save(user);

        return ResponseEntity.noContent().build();
    }

    private UserRole parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        try {
            return UserRole.valueOf(rawRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported role. Use USER, PUBLISHER or ADMIN");
        }
    }

    public record UpdateUserRoleRequest(String role) {}
}
