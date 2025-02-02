package bingol.campus.comment.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommentDTO {
    private long id;
    private String username;
    private String profilePhoto;

    private long postId;

    private String content;
    private long storyId;

    private LocalDateTime createdAt;
}
