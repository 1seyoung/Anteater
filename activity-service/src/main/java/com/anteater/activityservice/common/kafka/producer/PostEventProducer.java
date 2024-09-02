package com.anteater.activityservice.common.kafka.producer;

import com.anteater.activityservice.post.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostEventProducer {

    private static final String TOPIC = "post-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPostCreatedEvent(Post post) {
        PostEvent event = new PostEvent("CREATED", post);
        kafkaTemplate.send(TOPIC, post.getAuthorId().toString(), event);
    }

    public void sendPostUpdatedEvent(Post post) {
        PostEvent event = new PostEvent("UPDATED", post);
        kafkaTemplate.send(TOPIC, post.getAuthorId().toString(), event);
    }

    public void sendPostDeletedEvent(Long postId, Long authorId) {
        PostEvent event = new PostEvent("DELETED", postId, authorId);
        kafkaTemplate.send(TOPIC, authorId.toString(), event);
    }

    private static class PostEvent {
        private String eventType;
        private Long postId;
        private Long authorId;
        private String stockIsin;
        private String content;

        public PostEvent(String eventType, Post post) {
            this.eventType = eventType;
            this.postId = post.getId();
            this.authorId = post.getAuthorId();
            this.stockIsin = post.getStockIsin();
            this.content = post.getContent();
        }

        public PostEvent(String eventType, Long postId, Long authorId) {
            this.eventType = eventType;
            this.postId = postId;
            this.authorId = authorId;
        }

        // Getters and setters
    }
}