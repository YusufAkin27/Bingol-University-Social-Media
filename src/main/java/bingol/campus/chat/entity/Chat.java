package bingol.campus.chat.entity;

import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student1_id", nullable = false)
    private Student student1;

    @OneToOne
    @JoinColumn(name = "student2_id", nullable = false)
    private Student student2;

    @OneToOne
    @JoinColumn(name = "last_message_id")
    private Message lastMessage; // Sohbetteki son mesaj

    @OneToOne(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private PinnedMessage pinnedMessage;
}
