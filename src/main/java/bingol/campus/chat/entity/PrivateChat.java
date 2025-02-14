package bingol.campus.chat.entity;

import bingol.campus.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "private_chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChat extends Chat {
    @ManyToOne
    @JoinColumn(name = "participant1_id", nullable = false)
    private ChatParticipant participant1;

    @ManyToOne
    @JoinColumn(name = "participant2_id", nullable = false)
    private ChatParticipant participant2;
}


