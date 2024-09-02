package com.anteater.activityservice.post.dto;

import com.anteater.activityservice.post.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostResponse {
    private Long id;
    private Long authorId;
    private String stockIsin;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse fromEntity(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setAuthorId(post.getAuthorId());
        response.setStockIsin(post.getStockIsin());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }
}
