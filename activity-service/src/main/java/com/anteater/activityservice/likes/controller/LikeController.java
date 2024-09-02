package com.anteater.activityservice.likes.controller;

import com.anteater.activityservice.likes.dto.LikeCountResponse;
import com.anteater.activityservice.likes.dto.UserLikeStatusResponse;
import com.anteater.activityservice.likes.entity.Like;
import com.anteater.activityservice.likes.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{targetType}/{targetId}/user/{userId}")
    public ResponseEntity<?> likeTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @PathVariable Long userId) {
        Like.TargetType type = Like.TargetType.valueOf(targetType.toUpperCase());
        likeService.likeTarget(userId, targetId, type);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{targetType}/{targetId}/user/{userId}")
    public ResponseEntity<?> unlikeTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @PathVariable Long userId) {
        Like.TargetType type = Like.TargetType.valueOf(targetType.toUpperCase());
        likeService.unlikeTarget(userId, targetId, type);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{targetType}/{targetId}/count")
    public ResponseEntity<LikeCountResponse> getLikeCount(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        Like.TargetType type = Like.TargetType.valueOf(targetType.toUpperCase());
        long count = likeService.getLikeCount(targetId, type);
        return ResponseEntity.ok(new LikeCountResponse(count));
    }

    @GetMapping("/{targetType}/{targetId}/user/{userId}")
    public ResponseEntity<UserLikeStatusResponse> hasUserLikedTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @PathVariable Long userId) {
        Like.TargetType type = Like.TargetType.valueOf(targetType.toUpperCase());
        boolean hasLiked = likeService.hasUserLikedTarget(userId, targetId, type);
        return ResponseEntity.ok(new UserLikeStatusResponse(hasLiked));
    }
}