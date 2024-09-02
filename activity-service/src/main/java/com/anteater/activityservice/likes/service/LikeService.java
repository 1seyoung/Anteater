package com.anteater.activityservice.likes.service;

import com.anteater.activityservice.common.kafka.producer.LikeEventProducer;
import com.anteater.activityservice.likes.entity.Like;
import com.anteater.activityservice.likes.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeEventProducer likeEventProducer;

    @Transactional
    public void likeTarget(Long userId, Long targetId, Like.TargetType targetType) {
        if (!likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)) {
            Like like = Like.create(userId, targetId, targetType);
            likeRepository.save(like);
            likeEventProducer.sendLikeCreatedEvent(userId, targetId, targetType);
        }
    }

    @Transactional
    public void unlikeTarget(Long userId, Long targetId, Like.TargetType targetType) {
        likeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    likeEventProducer.sendLikeDeletedEvent(userId, targetId, targetType);
                });
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long targetId, Like.TargetType targetType) {
        return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikedTarget(Long userId, Long targetId, Like.TargetType targetType) {
        return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }
}