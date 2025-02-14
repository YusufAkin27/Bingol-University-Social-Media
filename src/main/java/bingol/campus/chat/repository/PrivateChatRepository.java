package bingol.campus.chat.repository;

import bingol.campus.chat.entity.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateChatRepository extends JpaRepository<PrivateChat,Long> {
}
