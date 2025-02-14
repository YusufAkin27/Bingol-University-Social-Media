package bingol.campus.chat.repository;

import bingol.campus.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
