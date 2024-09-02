package com.anteater.activityservice.likes.entity;

import jakarta.persistence.*;
import org.hibernate.tool.schema.TargetType;

import java.time.LocalDateTime;


@Entity
@Table(name = "likes", indexes = {
        @Index(name = "idx_likes_user_id_created_at", columnList = "userId, createdAt DESC"),
        @Index(name = "idx_likes_target_id_type_created_at", columnList = "targetId, targetType, createdAt DESC")
})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum TargetType {
        POST, COMMENT
    }

    // 생성 메서드
    public static Like create(Long userId, Long targetId, TargetType targetType) {
        Like like = new Like();
        like.userId = userId;
        like.targetId = targetId;
        like.targetType = targetType;
        like.createdAt = LocalDateTime.now();
        return like;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getTargetId() { return targetId; }
    public TargetType getTargetType() { return targetType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}