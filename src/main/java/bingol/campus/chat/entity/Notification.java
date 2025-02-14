package bingol.campus.chat.entity;

import bingol.campus.chat.entity.Chat;
import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private Student receiver;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Student sender;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead = false;
}