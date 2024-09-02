package com.anteater.activityservice.post.dto;

import lombok.Data;

@Data
public class PostCreateRequest {
    private Long authorId;
    private String stockIsin;
    private String content;
}