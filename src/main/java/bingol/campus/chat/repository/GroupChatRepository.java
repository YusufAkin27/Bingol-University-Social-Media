package bingol.campus.chat.repository;

import bingol.campus.chat.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRepository extends JpaRepository<GroupChat,Long> {
}
