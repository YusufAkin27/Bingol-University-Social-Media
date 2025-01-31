package bingol.campus.story.core.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryDTO {
    private long storyId;
    private String username;
    private String photo;
    private int score;
}
