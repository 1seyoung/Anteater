package com.anteater.activityservice.comment.service;

import com.anteater.activityservice.comment.entity.Comment;
import com.anteater.activityservice.comment.repository.CommentRepository;
import com.anteater.activityservice.common.kafka.producer.CommentEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentEventProducer commentEventProducer;

    @Transactional
    public Comment createComment(Long postId, Long authorId, String content) {
        Comment comment = Comment.create(postId, authorId, content);
        Comment savedComment = commentRepository.save(comment);
        commentEventProducer.sendCommentCreatedEvent(savedComment);
        return savedComment;
    }

    @Transactional(readOnly = true)
    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @Transactional
    public Comment updateComment(Long commentId, String newContent) {
        Comment comment = getComment(commentId);
        comment.updateContent(newContent);
        Comment updatedComment = commentRepository.save(comment);
        commentEventProducer.sendCommentUpdatedEvent(updatedComment);
        return updatedComment;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = getComment(commentId);
        commentRepository.deleteById(commentId);
        commentEventProducer.sendCommentDeletedEvent(commentId, comment.getAuthorId());
    }

    @Transactional(readOnly = true)
    public Page<Comment> getCommentsForPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }
}