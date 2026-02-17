package com.example.gameforum.config;

import com.example.gameforum.security.RestAccessDeniedHandler;
import com.example.gameforum.security.RestAuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final RestAuthEntryPoint authEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthFilter jwtFilter, RestAuthEntryPoint authEntryPoint, RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtFilter = jwtFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/forum/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/games/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/home",
                                "/home.html",
                                "/catalog",
                                "/catalog.html",
                                "/game-topics",
                                "/game-topics.html",
                                "/topic-discussion",
                                "/topic-discussion.html",
                                "/auth/**",
                                "/templates/**",
                                "/fragments/**",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/uploads/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()

                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/games/*/comments").hasAnyRole("USER", "PUBLISHER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/games/*/rating").hasAnyRole("USER", "PUBLISHER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/forum/**").hasAnyRole("USER", "PUBLISHER", "ADMIN")

                        .requestMatchers("/api/publisher/**").hasAnyRole("PUBLISHER", "ADMIN")

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
