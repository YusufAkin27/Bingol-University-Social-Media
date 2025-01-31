package bingol.campus.story.core.converter;

import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.story.core.response.StoryDetails;
import bingol.campus.story.entity.Story;
import org.springframework.data.domain.Pageable;

public interface StoryConverter {

    StoryDetails toDetails(Story story, Pageable pageable);

    StoryDTO toDto(Story story);
}
