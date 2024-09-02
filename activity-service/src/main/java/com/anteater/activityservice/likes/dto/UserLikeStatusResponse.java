package com.anteater.activityservice.likes.dto;

import lombok.Data;

@Data
public class UserLikeStatusResponse {
    private boolean hasLiked;

    public UserLikeStatusResponse(boolean hasLiked) {
        this.hasLiked = hasLiked;
    }
}