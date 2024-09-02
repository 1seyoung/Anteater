package com.anteater.newsfeedservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "newsfeed_items", indexes = {
        @Index(name = "idx_newsfeed_username_created_at", columnList = "userName, createdAt DESC"),
        @Index(name = "idx_newsfeed_event_type", columnList = "eventType")
})
@Getter
@Setter
public class NewsfeedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "stock_isin")
    private String stockIsin;

    @Column(name = "parent_id")
    private Long parentId;
}