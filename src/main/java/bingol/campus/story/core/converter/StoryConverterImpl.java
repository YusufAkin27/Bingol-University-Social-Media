package bingol.campus.story.core.converter;

import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.comment.entity.Comment;
import bingol.campus.comment.repository.CommentRepository;
import bingol.campus.like.core.converter.LikeConverter;
import bingol.campus.post.core.response.CommentDetailsDTO;
import bingol.campus.story.core.response.StoryDTO;
import bingol.campus.story.core.response.StoryDetails;
import bingol.campus.story.entity.Story;
import bingol.campus.student.core.converter.StudentConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StoryConverterImpl implements StoryConverter {

    private final CommentConverter commentConverter;
    private final CommentRepository commentRepository;
    private final StudentConverter studentConverter;
    private final LikeConverter likeConverter;



    @Override
    public StoryDetails toDetails(Story story, Pageable pageable) {
        // Hikaye detaylarını oluştur
        StoryDetails storyDetails = StoryDetails.builder()
                .id(story.getId())
                .username(story.getStudent().getUsername()) // Hikayeyi paylaşan öğrencinin adı
                .photoUrl(story.getPhoto()) // Fotoğraf URL'si
                .createdAt(story.getCreatedAt()) // Oluşturulma tarihi
                .expiresAt(story.getExpiresAt()) // Sonlanma tarihi
                .isActive(story.isActive()) // Hikayenin aktiflik durumu
                .likeCount(story.getLikes().size()) // Beğeni sayısı
                .viewing(story.getViewers().stream()
                        .map(storyViewer -> studentConverter.toSearchAccountDTO(storyViewer.getStudent())) // Görüntüleyen öğrenciler
                        .collect(Collectors.toList()))
                .likes(story.getLikes().stream().filter(like -> like.getStudent().getIsActive()).map(likeConverter::toDetails).toList())

                .build();

        // Sayfalı yorumları almak
        Page<Comment> commentsPage = commentRepository.findByStory(story, pageable);
        List<CommentDetailsDTO> commentDetailsDTOS = commentsPage.getContent().stream()
                .map(commentConverter::toDetails)
                .collect(Collectors.toList());

        // Hikayenin yorumlarını ekleyin
        storyDetails.setComments(commentDetailsDTOS);

        return storyDetails;
    }


    @Override
    public StoryDTO toDto(Story story) {
        StoryDTO storyDTO=new StoryDTO();
        storyDTO.setPhoto(story.getPhoto());
        storyDTO.setUsername(story.getStudent().getUsername());
        storyDTO.setStoryId(story.getId());
        return storyDTO;
    }
}
