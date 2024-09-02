package com.anteater.activityservice.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter @Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 12)
    private String stockIsin;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Post create(Long authorId, String stockIsin, String content) {
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setStockIsin(stockIsin);
        post.setContent(content);
        LocalDateTime now = LocalDateTime.now();
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        return post;
    }

    public void update(String content) {
        this.setContent(content);
        this.setUpdatedAt(LocalDateTime.now());
    }
}
