package bingol.campus.comment.business.abstracts;

import bingol.campus.comment.core.exception.CommentNotFoundException;
import bingol.campus.comment.core.exception.UnauthorizedCommentException;
import bingol.campus.comment.core.response.CommentDTO;
import bingol.campus.followRelation.core.exceptions.BlockingBetweenStudent;
import bingol.campus.post.core.exceptions.PostNotFoundException;
import bingol.campus.post.core.exceptions.PostNotIsActiveException;
import bingol.campus.response.DataResponseMessage;
import bingol.campus.response.ResponseMessage;
import bingol.campus.story.core.exceptions.NotFollowingException;
import bingol.campus.story.core.exceptions.StoryNotActiveException;
import bingol.campus.story.core.exceptions.StoryNotFoundException;
import bingol.campus.student.exceptions.StudentNotFoundException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    ResponseMessage addCommentToStory(String username, Long storyId, String content) throws StudentNotFoundException, StoryNotFoundException, StoryNotActiveException, BlockingBetweenStudent, NotFollowingException;

    ResponseMessage addCommentToPost(String username, Long postId, String content) throws PostNotFoundException, StudentNotFoundException, PostNotIsActiveException, NotFollowingException, BlockingBetweenStudent;

    ResponseMessage deleteComment(String username, Long commentId) throws CommentNotFoundException, StudentNotFoundException, UnauthorizedCommentException;

    DataResponseMessage<CommentDTO> getCommentDetails(String username, Long commentId) throws UnauthorizedCommentException, CommentNotFoundException, StudentNotFoundException;


    DataResponseMessage<List<CommentDTO>> searchUserInStoryComments(String username, Long storyId, String username1) throws UnauthorizedCommentException, StudentNotFoundException, StoryNotFoundException;

    DataResponseMessage<List<CommentDTO>> searchUserInPostComments(String username, Long postId, String username1) throws PostNotIsActiveException, NotFollowingException, BlockingBetweenStudent, UnauthorizedCommentException, StudentNotFoundException, PostNotFoundException;

    DataResponseMessage<List<CommentDTO>> getUserComments(String username, Pageable pageRequest) throws StudentNotFoundException;

    DataResponseMessage<List<CommentDTO>> getStoryComments(String username, Long storyId, Pageable pageable) throws NotFollowingException, BlockingBetweenStudent, StoryNotActiveException, StudentNotFoundException, StoryNotFoundException;

    DataResponseMessage<List<CommentDTO>> getPostComments(String username, Long postId, Pageable pageable) throws StudentNotFoundException, PostNotFoundException, PostNotIsActiveException, NotFollowingException, BlockingBetweenStudent;
}
