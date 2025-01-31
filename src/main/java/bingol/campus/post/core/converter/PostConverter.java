package bingol.campus.post.core.converter;

import bingol.campus.post.core.request.CreatePostRequest;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.post.entity.Post;

public interface PostConverter {
    PostDTO toDto(Post post);
    Post createConverter(CreatePostRequest createPostRequest);
}
