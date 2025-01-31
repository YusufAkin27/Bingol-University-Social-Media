package bingol.campus.chat.entity;

import bingol.campus.chat.entity.enums.MediaType;
import bingol.campus.student.entity.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_media")
public class ChatMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    @JsonIgnore
    private Chat chat; // Medyanın ait olduğu sohbet

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Student uploadedBy; // Medyayı yükleyen kişi

    private String mediaUrl; // Medya dosyasının URL'si

    @Enumerated(EnumType.STRING)
    private MediaType mediaType; // Medya türü (IMAGE, VIDEO)

    private LocalDateTime uploadedAt; // Medyanın yüklenme zamanı

    @OneToOne
    @JoinColumn(name = "message_id")
    private Message message; // Medyanın altına gönderilen mesaj

    private boolean isTemporary = false; // Medyanın tek görmelik olup olmadığını belirler

}
