package com.anteater.activityservice.comment.dto;


import lombok.Data;

@Data
public class CommentCreateRequest {
    private Long postId;
    private Long authorId;
    private String content;
}