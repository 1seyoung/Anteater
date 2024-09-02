package com.anteater.activityservice.comment.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post_id_created_at", columnList = "postId, createdAt DESC"),
        @Index(name = "idx_comments_author_id_created_at", columnList = "authorId, createdAt DESC")
})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Comment create(Long postId, Long authorId, String content) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.authorId = authorId;
        comment.content = content;
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = comment.createdAt;
        return comment;
    }

    // 내용 수정 메서드
    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}