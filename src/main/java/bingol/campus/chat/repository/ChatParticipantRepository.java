package bingol.campus.chat.repository;

import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.ChatParticipant;
import bingol.campus.chat.entity.PrivateChat;
import bingol.campus.student.entity.Student;
import com.google.api.gax.rpc.ServerStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant,Long> {
    Optional<ChatParticipant> findByChatAndStudent(Chat chat, Student student);

    List<ChatParticipant> findByChatId(Long chatId);
}
