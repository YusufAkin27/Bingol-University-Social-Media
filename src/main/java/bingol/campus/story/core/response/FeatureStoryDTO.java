package bingol.campus.story.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeatureStoryDTO {
    private long featureStoryId;
    private String coverPhoto;
    private String title;
    private List<StoryDTO>storyDTOS;
}
