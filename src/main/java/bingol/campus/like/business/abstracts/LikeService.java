package bingol.campus.like.business.abstracts;

import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.like.core.exceptions.AlreadyLikedException;
import bingol.campus.like.core.exceptions.PostNotFoundLikeException;
import bingol.campus.like.core.exceptions.StoryNotFoundLikeException;
import bingol.campus.post.core.exceptions.PostNotFoundException;
import bingol.campus.post.core.exceptions.PostNotIsActiveException;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.story.core.exceptions.NotFollowingException;
import bingol.campus.story.core.exceptions.StoryNotActiveException;
import bingol.campus.story.core.exceptions.StoryNotFoundException;
import bingol.campus.story.core.exceptions.StudentProfilePrivateException;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.student.core.response.SearchAccountDTO;
import bingol.campus.student.exceptions.StudentNotFoundException;

import java.util.List;

public interface LikeService {
    ResponseMessage likeStory(String username, Long storyId) throws StoryNotFoundException, StudentNotFoundException, StoryNotActiveException, BlockingBetweenStudent, NotFollowingException, AlreadyLikedException, StudentProfilePrivateException;

    ResponseMessage likePost(String username, Long postId) throws StudentNotFoundException, PostNotFoundException, PostNotIsActiveException, BlockingBetweenStudent, NotFollowingException, AlreadyLikedException, StudentProfilePrivateException;

    ResponseMessage unlikeStory(String username, Long storyId) throws StoryNotFoundException, StudentNotFoundException, StoryNotFoundLikeException;

    ResponseMessage unlikePost(String username, Long postId) throws StudentNotFoundException, PostNotFoundException, PostNotFoundLikeException;

    DataResponseMessage<List<StoryDTO>> getUserLikedStories(String username) throws StudentNotFoundException;

    DataResponseMessage<List<PostDTO>> getUserLikedPosts(String username) throws StudentNotFoundException;

    DataResponseMessage<List<PostDTO>> getPostLikesAfter(Long postId, String dateTime) throws PostNotFoundException;


    DataResponseMessage<SearchAccountDTO> searchUserInPostLikes(String username, Long postId, String username1) throws PostNotFoundException, StudentNotFoundException, NotFollowingException, BlockingBetweenStudent, StudentProfilePrivateException;

    DataResponseMessage<SearchAccountDTO> searchUserInStoryLikes(String username, Long storyId, String username1) throws StudentNotFoundException, StoryNotFoundException, NotFollowingException, BlockingBetweenStudent, StudentProfilePrivateException;
}
