package bingol.campus.chat.repository;

import bingol.campus.chat.entity.Chat;
import bingol.campus.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findByChat(Chat chat);
}
