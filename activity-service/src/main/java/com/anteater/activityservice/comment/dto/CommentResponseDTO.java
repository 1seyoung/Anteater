package com.anteater.activityservice.comment.dto;

import com.anteater.activityservice.comment.entity.Comment;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class CommentResponseDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponseDTO fromEntity(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setAuthorId(comment.getAuthorId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}
