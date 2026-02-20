package com.example.gameforum.forum;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "forum_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(nullable = false, length = 80)
    private String author;

    @Column(name = "avatar_color", nullable = false, length = 20)
    private String avatarColor;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "image_urls_text", nullable = false, columnDefinition = "text")
    private String imageUrlsText;

    @Column(name = "parent_message_id")
    private Long parentMessageId;

    @Column(name = "quoted_message_id")
    private Long quotedMessageId;

    @Column(nullable = false)
    private Integer likes;

    @Column(nullable = false)
    private Integer dislikes;

    @Column(nullable = false)
    private Integer replies;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "edited_at")
    private OffsetDateTime editedAt;
}
