package com.anteater.activityservice.common.kafka.producer;

import com.anteater.activityservice.likes.entity.Like;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LikeEventProducer {

    private static final String TOPIC = "like-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendLikeCreatedEvent(Long userId, Long targetId, Like.TargetType targetType) {
        LikeEvent event = new LikeEvent("CREATED", userId, targetId, targetType);
        kafkaTemplate.send(TOPIC, userId.toString(), event);
    }

    public void sendLikeDeletedEvent(Long userId, Long targetId, Like.TargetType targetType) {
        LikeEvent event = new LikeEvent("DELETED", userId, targetId, targetType);
        kafkaTemplate.send(TOPIC, userId.toString(), event);
    }

    private static class LikeEvent {
        private String eventType;
        private Long userId;
        private Long targetId;
        private Like.TargetType targetType;

        public LikeEvent(String eventType, Long userId, Long targetId, Like.TargetType targetType) {
            this.eventType = eventType;
            this.userId = userId;
            this.targetId = targetId;
            this.targetType = targetType;
        }

        // Getters and setters
    }
}