package bingol.campus.post.core.converter;

import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.post.core.request.CreatePostRequest;
import bingol.campus.post.core.response.PostDTO;
import bingol.campus.post.entity.Post;
import bingol.campus.student.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PostConverterImpl implements PostConverter {
    private final CommentConverter commentConverter;

    @Override
    public PostDTO toDto(Post post) {
        return PostDTO.builder()
                .postId(post.getId())
                .like(post.getLikes().size())
                .comment(post.getComments().size())
                .popularityScore(post.getPopularityScore())
                .content(post.getPhotos())
                .createdAt(LocalDateTime.now())
                .description(post.getDescription())
                .tagAPerson(post.getTaggedPersons().stream().map(Student::getUsername).toList())
                .location(post.getLocation())
                .username(post.getStudent().getUsername())
                .build();

    }

    @Override
    public Post createConverter(CreatePostRequest createPostRequest) {
        return Post.builder()
                .comments(null)
                .createdAt(LocalDateTime.now())
                .description(createPostRequest.getDescription())
                .likes(null)
                .isActive(true)
                .isDelete(false)
                .location(createPostRequest.getLocation())
                .build();
    }
}
