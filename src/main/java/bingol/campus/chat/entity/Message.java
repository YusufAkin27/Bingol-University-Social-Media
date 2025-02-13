package bingol.campus.chat.entity;

import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MessageType type; // TEXT, IMAGE, VIDEO, FILE, STICKER

    private String content; // Metin mesajı

    private LocalDateTime sentTime;
    private boolean isRead;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Student sender;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @OneToOne
    @JoinColumn(name = "replied_message_id") // Yanıtlanan mesaj
    private Message repliedMessage;

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatMedia mediaFile;
}
