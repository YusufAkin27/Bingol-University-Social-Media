package bingol.campus.student.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeStoryDTO {
    private List<Long> storyId;
    private long studentId;
    private String username;
    private List<String>photos;
    private String profilePhoto;
    private boolean isVisited;

}
