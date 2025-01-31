package bingol.campus.story.core.response;

import bingol.campus.like.entity.Like;
import bingol.campus.post.core.response.CommentDetailsDTO;
import bingol.campus.post.core.response.LikeDetailsDTO;
import bingol.campus.student.core.response.SearchAccountDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoryDetails {
    private long id;
    private String username; // Hikayeyi paylaşan kullanıcının adı
    private String photoUrl; // Hikayenin fotoğraf URL'si
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isActive;
    private long likeCount;
    private List<CommentDetailsDTO> comments;
    private List<SearchAccountDTO> viewing;
    private List<LikeDetailsDTO>likes;

    public boolean isStoryActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(expiresAt);
    }

    @Override
    public String toString() {
        return "StoryDetails{" +
                "username='" + username + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", expiresAt='" + expiresAt + '\'' +
                ", isActive=" + isActive +
                ", likeCount=" + likeCount +
                ", comments=" + comments +
                '}';
    }
}
