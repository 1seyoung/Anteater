package com.anteater.newsfeedservice.kafka.event;


import lombok.Data;

@Data
public class CommentEvent {
    private Long commentId;
    private Long postId;
    private String userId;
    private String content;
}