package com.anteater.newsfeedservice.kafka.event;

import lombok.Data;

@Data
public class PostEvent {
    private Long postId;
    private Long authorId;
    private String content;
    private String stockIsin;
}