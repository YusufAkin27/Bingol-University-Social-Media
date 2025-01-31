package bingol.campus.student.business.abstracts;

import bingol.campus.student.entity.Student;
import bingol.campus.student.repository.StudentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PopularityScoreService {

    private final StudentRepository studentRepository;

    public PopularityScoreService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Popülerlik skorunu hesapla
    private int calculatePopularityScore(Student student) {
        int followersCount = student.getFollowers().size(); // Takipçi sayısı
        int likesCount = student.getLikes().size(); // Beğenilen gönderiler
        int commentsCount = student.getComments().size(); // Yorumlar
        int postsCount = student.getPost().size(); // Paylaşılan gönderiler
        int storiesCount = student.getStories().size(); // Paylaşılan hikayeler
        int featuredStoriesCount = student.getFeaturedStories().size(); // Öne çıkan hikayeler

        // Popülerlik skorunu belirlemek için bir formül kullanabiliriz
        int score = followersCount * 5 + likesCount * 2 + commentsCount + postsCount * 3 + storiesCount * 2 + featuredStoriesCount * 4;

        return score;
    }

    // Bu fonksiyon öğrenci için popülerlik skorunu günceller
    @Transactional
    @Scheduled(cron = "0 0 5 * * ?") // Her gün saat 05:00'te çalışır
    public void updatePopularityScores() {
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            // Lazy ilişkileri yüklemek için boyutlarını çağırıyoruz
            student.getFollowers().size(); // Takipçileri yükle
            student.getLikes().size(); // Beğenileri yükle
            student.getComments().size(); // Yorumları yükle
            student.getPost().size(); // Gönderileri yükle
            student.getStories().size(); // Hikayeleri yükle
            student.getFeaturedStories().size(); // Öne çıkan hikayeleri yükle

            // Popülerlik skorunu hesapla
            int popularityScore = calculatePopularityScore(student);
            student.setPopularityScore(popularityScore);
            studentRepository.save(student); // Veritabanına kaydediyoruz
        }
    }

}
