package com.anteater.activityservice.likes.dto;

import lombok.Data;

@Data
public class LikeCountResponse {
    private long count;

    public LikeCountResponse(long count) {
        this.count = count;
    }
}