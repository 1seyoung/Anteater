package com.anteater.activityservice.comment.controller;


import com.anteater.activityservice.comment.dto.CommentCreateRequest;
import com.anteater.activityservice.comment.dto.CommentResponseDTO;
import com.anteater.activityservice.comment.dto.CommentUpdateRequest;
import com.anteater.activityservice.comment.entity.Comment;
import com.anteater.activityservice.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(@RequestBody CommentCreateRequest request) {
        Comment comment = commentService.createComment(request.getPostId(), request.getAuthorId(), request.getContent());
        return ResponseEntity.ok(CommentResponseDTO.fromEntity(comment));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> getComment(@PathVariable Long commentId) {
        Comment comment = commentService.getComment(commentId);
        return ResponseEntity.ok(CommentResponseDTO.fromEntity(comment));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdateRequest request) {
        Comment comment = commentService.updateComment(commentId, request.getContent());
        return ResponseEntity.ok(CommentResponseDTO.fromEntity(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsForPost(
            @PathVariable Long postId,
            Pageable pageable) {
        Page<Comment> comments = commentService.getCommentsForPost(postId, pageable);
        Page<CommentResponseDTO> commentDTOs = comments.map(CommentResponseDTO::fromEntity);
        return ResponseEntity.ok(commentDTOs);
    }
}