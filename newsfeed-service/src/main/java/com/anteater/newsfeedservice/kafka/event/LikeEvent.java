package com.anteater.newsfeedservice.kafka.event;

import lombok.Data;

@Data
public class LikeEvent {
    private Long userId;
    private Long targetId;
    private String targetType;
    private Long postId;
}