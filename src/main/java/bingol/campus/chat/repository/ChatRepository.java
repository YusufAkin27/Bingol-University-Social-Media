package bingol.campus.chat.repository;

import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.PrivateChat;
import bingol.campus.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat,Long>{

    @Query("SELECT pc FROM PrivateChat pc JOIN pc.participants p1 JOIN pc.participants p2 " +
            "WHERE (p1 = :sender AND p2 = :receiver) OR (p1 = :receiver AND p2 = :sender)")
    Optional<PrivateChat> findByParticipants(@Param("sender") Student sender, @Param("receiver") Student receiver);}
