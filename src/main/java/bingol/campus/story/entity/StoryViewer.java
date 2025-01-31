package bingol.campus.story.entity;

import bingol.campus.student.entity.Student; // Öğrenci entity'si
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoryViewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // StoryViewer ID'si

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student; // Görüntüleyen öğrenci

    @ManyToOne
    @JoinColumn(name = "story_id", nullable = false)
    private Story story; // Görüntülenen hikaye

    private LocalDateTime viewedAt = LocalDateTime.now(); // Hikaye görüntüleme zamanı
}
