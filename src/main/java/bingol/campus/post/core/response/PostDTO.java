package bingol.campus.post.core.response;

import bingol.campus.comment.core.response.CommentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDTO {
    private String username;
    private List<String> content;
    private String description;
    private List<String>tagAPerson;
    private String location;
    private LocalDateTime createdAt; // Gönderinin oluşturulma tarihi

    private long like;
    private long comment;
    private long popularityScore;


}
