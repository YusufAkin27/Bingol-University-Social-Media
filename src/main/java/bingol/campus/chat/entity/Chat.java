package bingol.campus.chat.entity;

import bingol.campus.student.entity.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "chat_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chats")
@SuperBuilder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt; // Sohbetin oluşturulma zamanı

    private LocalDateTime lastActiveAt; // Son aktif olma zamanı

    private boolean isDeleted = false; // Sohbet silinmiş mi?

    private boolean isArchived = false; // Sohbet arşivlenmiş mi?

    @OneToOne
    @JoinColumn(name = "last_message_id")
    private Message lastMessage; // Son mesaj

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> messages; // Sohbete ait mesajlar

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChatParticipant> participants=new ArrayList<>(); // Sohbete katılan kullanıcılar

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChatMedia> mediaFiles; // Sohbete eklenen medya dosyaları

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> pinnedMessages; // Sabitlenmiş mesajlar
}
