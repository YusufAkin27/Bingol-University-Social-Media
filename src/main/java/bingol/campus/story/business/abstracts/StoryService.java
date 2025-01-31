package bingol.campus.story.business.abstracts;

import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.post.core.response.CommentDetailsDTO;
import bingol.campus.post.core.response.LikeDetailsDTO;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.story.core.exceptions.StoryNotActiveException;
import bingol.campus.story.core.exceptions.*;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.story.core.response.StoryDetails;
import bingol.campus.student.core.response.SearchAccountDTO;
import bingol.campus.student.exceptions.StudentNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StoryService {
    ResponseMessage add(String username, MultipartFile photos) throws StudentNotFoundException, IOException;

    ResponseMessage delete(String username, Long storyId) throws StoryNotFoundException, StudentNotFoundException, OwnerStoryException;


    ResponseMessage featureStory(String username, Long storyId,Long featuredStoryId) throws StudentNotFoundException, StoryNotFoundException, OwnerStoryException, AlreadyFeaturedStoriesException, FeaturedStoryGroupNotFoundException;



    ResponseMessage extendStoryDuration(String username, Long storyId, int hours) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException, InvalidHourRangeException, FeaturedStoryModificationException;

    List<SearchAccountDTO> getStoryViewers(String username, Long storyId) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException;

    int getStoryViewCount(String username, Long storyId) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException;

    DataResponseMessage<List<StoryDTO>> getPopularStories(String username) throws StudentNotFoundException;

    DataResponseMessage<List<StoryDTO>> getUserActiveStories(String username, String username1) throws StudentNotFoundException, BlockingBetweenStudent, NotFollowingException;

    DataResponseMessage<List<CommentDetailsDTO>> getStoryComments(String username, Long storyId) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException;

    DataResponseMessage<StoryDTO> viewStory(String username, Long storyId) throws StoryNotFoundException, StoryNotActiveException, StudentNotFoundException, NotFollowingException, BlockingBetweenStudent;



    DataResponseMessage<List<LikeDetailsDTO>> getLike(String username, Long storyId) throws StudentNotFoundException, OwnerStoryException, StoryNotActiveException;


    DataResponseMessage<List<StoryDetails>> getStories(String username, Pageable pageable) throws StudentNotFoundException;


    DataResponseMessage<StoryDetails> getStoryDetails(String username, Long storyId, Pageable pageable) throws StudentNotFoundException, StoryNotFoundException, OwnerStoryException;
}
