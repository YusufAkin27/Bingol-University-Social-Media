package bingol.campus.story.core.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryDTO {
    private long storyId;
    private String profilePhoto;
    private String username;
    private long userId;
    private String photo;
    private int score;
}
