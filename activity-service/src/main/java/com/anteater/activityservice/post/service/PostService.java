package com.anteater.activityservice.post.service;

import com.anteater.activityservice.common.exception.PostModificationTimeExceededException;
import com.anteater.activityservice.common.kafka.producer.PostEventProducer;
import com.anteater.activityservice.post.entity.Post;
import com.anteater.activityservice.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostEventProducer postEventProducer;

    @Autowired
    public PostService(PostRepository postRepository, PostEventProducer postEventProducer) {
        this.postRepository = postRepository;
        this.postEventProducer = postEventProducer;
    }

    @Transactional
    public Post createPost(Long authorId, String stockIsin, String content) {
        Post post = Post.create(authorId, stockIsin, content);
        Post savedPost = postRepository.save(post);
        postEventProducer.sendPostCreatedEvent(savedPost);
        return savedPost;
    }

    @Transactional(readOnly = true)
    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public Post updatePost(Long postId, String newContent) {
        Post post = getPost(postId);

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceCreation = ChronoUnit.MINUTES.between(post.getCreatedAt(), now);

        if (minutesSinceCreation > 5) {
            throw new PostModificationTimeExceededException("Posts can only be modified within 5 minutes of creation.");
        }

        post.update(newContent);
        Post updatedPost = postRepository.save(post);
        postEventProducer.sendPostUpdatedEvent(updatedPost);
        return updatedPost;
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = getPost(postId);
        postRepository.deleteById(postId);
        postEventProducer.sendPostDeletedEvent(postId, post.getAuthorId());
    }

    @Transactional(readOnly = true)
    public Page<Post> getPostsByStock(String stockIsin, Pageable pageable) {
        return postRepository.findByStockIsin(stockIsin, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorId(authorId, pageable);
    }
}