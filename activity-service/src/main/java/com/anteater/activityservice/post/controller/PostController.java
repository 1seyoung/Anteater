package com.anteater.activityservice.post.controller;

import com.anteater.activityservice.common.exception.PostModificationTimeExceededException;
import com.anteater.activityservice.post.dto.PostCreateRequest;
import com.anteater.activityservice.post.dto.PostResponse;
import com.anteater.activityservice.post.dto.PostUpdateRequest;
import com.anteater.activityservice.post.entity.Post;
import com.anteater.activityservice.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity/post")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostCreateRequest request) {
        Post post = postService.createPost(request.getAuthorId(), request.getStockIsin(), request.getContent());
        return ResponseEntity.ok(PostResponse.fromEntity(post));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        Post post = postService.getPost(postId);
        return ResponseEntity.ok(PostResponse.fromEntity(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody PostUpdateRequest request) {
        try {
            Post post = postService.updatePost(postId, request.getContent());
            return ResponseEntity.ok(PostResponse.fromEntity(post));
        } catch (PostModificationTimeExceededException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stock/{stockIsin}")
    public ResponseEntity<Page<PostResponse>> getPostsByStock(@PathVariable String stockIsin, Pageable pageable) {
        Page<Post> posts = postService.getPostsByStock(stockIsin, pageable);
        Page<PostResponse> postResponses = posts.map(PostResponse::fromEntity);
        return ResponseEntity.ok(postResponses);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<PostResponse>> getPostsByAuthor(@PathVariable Long authorId, Pageable pageable) {
        Page<Post> posts = postService.getPostsByAuthor(authorId, pageable);
        Page<PostResponse> postResponses = posts.map(PostResponse::fromEntity);
        return ResponseEntity.ok(postResponses);
    }
}