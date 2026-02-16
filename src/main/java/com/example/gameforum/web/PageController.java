package com.example.gameforum.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/home", "/home.html", "/templates/home.html"})
    public String home() {
        return "home";
    }

    @GetMapping({"/catalog", "/catalog.html", "/templates/catalog.html"})
    public String catalog() {
        return "catalog";
    }

    @GetMapping({"/game-topics", "/game-topics.html", "/templates/game-topics.html"})
    public String gameTopics() {
        return "game-topics";
    }

    @GetMapping({"/topic-discussion", "/topic-discussion.html", "/templates/topic-discussion.html"})
    public String topicDiscussion() {
        return "topic-discussion";
    }

    @GetMapping({"/auth/login", "/auth/login.html", "/templates/auth/login.html"})
    public String login() {
        return "auth/login";
    }

    @GetMapping({"/auth/register", "/auth/register.html", "/templates/auth/register.html"})
    public String register() {
        return "auth/register";
    }

    @GetMapping({"/auth/profile", "/auth/profile.html", "/templates/auth/profile.html"})
    public String profile() {
        return "auth/profile";
    }

    @GetMapping({"/fragments/header", "/fragments/header.html"})
    public String headerFragment() {
        return "fragments/header";
    }

    @GetMapping({"/fragments/footer", "/fragments/footer.html"})
    public String footerFragment() {
        return "fragments/footer";
    }
}
