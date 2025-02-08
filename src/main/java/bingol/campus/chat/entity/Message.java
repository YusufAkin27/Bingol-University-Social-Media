package bingol.campus.chat.entity;

import bingol.campus.chat.entity.Chat;
import bingol.campus.student.entity.Student;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private Student sender; // Mesajı gönderen kişi

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    @JsonIgnore
    private Chat chat; // Mesajın ait olduğu sohbet

    private String content; // Mesaj içeriği (metin)
    private  boolean deletedForAll=false;

    private LocalDateTime timestamp; // Mesajın gönderildiği zaman

    private boolean isRead = false; // Mesajın okunup okunmadığını belirten durum

    @ManyToOne
    @JoinColumn(name = "response_to_message_id")
    private Message responseTo; // Yanıtlanan mesaj (Bu alan bir mesajın yanıtını tutacak)

    @OneToMany(mappedBy = "responseTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> responses; // Bu mesajın yanıtları (Yanıtlar veritabanında kaydedilecek)
}
