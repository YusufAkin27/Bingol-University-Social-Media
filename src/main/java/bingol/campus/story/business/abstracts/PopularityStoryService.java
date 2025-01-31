package bingol.campus.story.business;

import bingol.campus.story.entity.Story;
import bingol.campus.story.repository.StoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PopularityStoryService {

    private final StoryRepository storyRepository;

    public PopularityStoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    // Hikayenin popülerlik skorunu hesaplayan metod
    private long calculateStoryScore(Story story) {
        long viewersScore = story.getViewers().size() * 1; // Her görüntüleme için 1 puan
        long likesScore = story.getLikes().size() * 5; // Her beğeni için 5 puan
        long commentsScore = story.getComments().size() * 10; // Her yorum için 10 puan
        return viewersScore + likesScore + commentsScore; // Toplam skor
    }

    // Her gün saat 05:00'te hikayelerin popülerlik skorlarını günceller
    @Transactional
    @Scheduled(cron = "0 0 6 * * ?") // Her gün 05:00'te çalışır
    public void updateStoryPopularityScores() {
        List<Story> stories = storyRepository.findAll();
        for (Story story : stories) {
            story.setScore(calculateStoryScore(story));
            storyRepository.save(story);
        }
    }

    // En popüler hikayeleri belirli bir limit ile getirir
    public List<Story> getTopPopularStories(int limit) {
        return storyRepository.findAll()
                .stream()
                .sorted((s1, s2) -> Long.compare(s2.getScore(), s1.getScore())) // Skora göre sıralama
                .limit(limit) // Belirtilen sayı kadar hikaye getir
                .collect(Collectors.toList());
    }
}
