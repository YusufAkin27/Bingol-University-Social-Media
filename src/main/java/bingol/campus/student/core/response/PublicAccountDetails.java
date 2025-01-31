package bingol.campus.student.core.response;

import bingol.campus.post.core.response.PostDTO;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.student.entity.enums.Department;
import bingol.campus.student.entity.enums.Faculty;
import bingol.campus.student.entity.enums.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicAccountDetails  {
    private long id;
    private String fullName;
    private String username;
    private String profilePhoto;
    private String bio;
    private boolean isFollow;
    private List<String> commonFriends;
    private long followingCount;
    private long followerCount;
    private long postCount;
    private boolean isPrivate;
    private long popularityScore;
    private List<PostDTO> posts;         // Gönderi başlıkları veya içerikleri
    private List<String> stories;       // Hikayeler (Başlıklar veya içerik)
    private List<String> featuredStories; // Öne çıkan hikayeler

}
