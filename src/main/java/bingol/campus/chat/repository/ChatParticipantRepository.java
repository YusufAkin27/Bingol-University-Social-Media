package bingol.campus.chat.repository;

import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant,Long> {
    Optional<ChatParticipant> findByChatAndStudent(Chat chat, Student student);
}
