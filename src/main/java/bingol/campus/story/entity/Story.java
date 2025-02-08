package bingol.campus.story.entity;

import bingol.campus.comment.entity.Comment;
import bingol.campus.student.entity.Student;
import bingol.campus.like.entity.Like;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Hikaye ID'si

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Student student; // Hikayeyi paylaşan öğrenci

    private String photo; // Hikayenin içeriği (metin, görsel URL'si vb.)

    private LocalDateTime createdAt = LocalDateTime.now(); // Hikayenin oluşturulma tarihi

    private LocalDateTime expiresAt; // Hikayenin sonlanma tarihi (24 saat sonra vs.)

    private boolean isFeatured=false;

    private boolean isActive = true; // Hikayenin geçerli olup olmadığını belirten alan

    private long score;

    @ManyToOne
    @JoinColumn(name = "featured_story_id")
    private FeaturedStory featuredStory; // Eğer hikaye bir "öne çıkarılan hikaye" grubuna bağlıysa

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "story")
    private List<Like> likes = new ArrayList<>(); // Hikayelere yapılan beğeniler

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "story")
    private List<Comment> comments = new ArrayList<>(); // Hikayeye yapılan yorumlar

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "story")
    private List<StoryViewer> viewers = new ArrayList<>(); // Hikayeyi görüntüleyen kullanıcılar

    // Hikayenin aktif olup olmadığını kontrol eden ve güncelleyen metod
// Hikayenin aktif olup olmadığını kontrol eden ve güncelleyen metod
    @PrePersist
    @PreUpdate
    public void updateIsActive() {
        if (expiresAt != null) {
            this.isActive = LocalDateTime.now().isBefore(expiresAt);
        }

        // Hikayenin skorunu hesapla (1 görüntüleme = 1 puan, 1 beğeni = 5 puan, 1 yorum = 10 puan)
        this.score = calculateScore();
    }

    // Skoru hesaplayan yardımcı metod
    private long calculateScore() {
        long viewersScore = this.viewers.size() * 1; // Her görüntüleme için 1 puan
        long likesScore = this.likes.size() * 5; // Her beğeni için 5 puan
        long commentsScore = this.comments.size() * 10; // Her yorum için 10 puan

        return viewersScore + likesScore + commentsScore; // Toplam puan
    }

}
