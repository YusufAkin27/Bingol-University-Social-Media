package bingol.campus.chat.entity;

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
public class PinnedMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    private LocalDateTime pinnedAt;
}
