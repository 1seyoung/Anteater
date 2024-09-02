package com.anteater.activityservice.common.kafka.producer;

import com.anteater.activityservice.comment.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommentEventProducer {

    private static final String TOPIC = "comment-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCommentCreatedEvent(Comment comment) {
        CommentEvent event = new CommentEvent("CREATED", comment);
        kafkaTemplate.send(TOPIC, String.valueOf(comment.getAuthorId()), event);
    }

    public void sendCommentUpdatedEvent(Comment comment) {
        CommentEvent event = new CommentEvent("UPDATED", comment);
        kafkaTemplate.send(TOPIC, String.valueOf(comment.getAuthorId()), event);
    }

    public void sendCommentDeletedEvent(Long commentId, Long authorId) {
        CommentEvent event = new CommentEvent("DELETED", commentId, authorId);
        kafkaTemplate.send(TOPIC, String.valueOf(authorId), event);
    }

    private static class CommentEvent {
        private String eventType;
        private Long commentId;
        private Long postId;
        private Long authorId;
        private String content;

        public CommentEvent(String eventType, Comment comment) {
            this.eventType = eventType;
            this.commentId = comment.getId();
            this.postId = comment.getPostId();
            this.authorId = comment.getAuthorId();
            this.content = comment.getContent();
        }

        public CommentEvent(String eventType, Long commentId, Long authorId) {
            this.eventType = eventType;
            this.commentId = commentId;
            this.authorId = authorId;
        }

        // Getters and setters
    }
}