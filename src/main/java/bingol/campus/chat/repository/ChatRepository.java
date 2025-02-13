package bingol.campus.chat.repository;

import bingol.campus.chat.entity.Chat;
import bingol.campus.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat,Long> {
    List<Chat> findByParticipantsContaining(Student sender);
}
