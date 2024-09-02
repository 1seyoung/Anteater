package com.anteater.activityservice.likes.repository;

import com.anteater.activityservice.likes.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, Like.TargetType targetType);
    long countByTargetIdAndTargetType(Long targetId, Like.TargetType targetType);
    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, Like.TargetType targetType);
}