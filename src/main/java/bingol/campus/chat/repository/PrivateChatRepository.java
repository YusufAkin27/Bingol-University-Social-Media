package bingol.campus.chat.repository;

import bingol.campus.chat.entity.PrivateChat;
import bingol.campus.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, Long> {

    @Query("SELECT pc FROM PrivateChat pc " +
            "JOIN pc.participants p1 " +
            "JOIN pc.participants p2 " +
            "WHERE p1.student = :sender AND p2.student = :receiver")
    Optional<PrivateChat> findByParticipants(Student sender, Student receiver);

}
