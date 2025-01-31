package bingol.campus.comment.core.converter;

import bingol.campus.comment.core.response.CommentDTO;
import bingol.campus.comment.entity.Comment;
import bingol.campus.post.core.response.CommentDetailsDTO;
import org.springframework.stereotype.Component;

@Component
public class CommentConverterImpl implements CommentConverter {
    @Override
    public CommentDTO toDto(Comment comment) {
        return CommentDTO.builder()
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .postId(comment.getPost().getId())
                .username(comment.getStudent().getUsername())
                .build();
    }

    @Override
    public CommentDetailsDTO toDetails(Comment comment) {
        return CommentDetailsDTO.builder()
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getStudent().getId())
                .username(comment.getStudent().getUsername())
                .build();

    }
}
