package bingol.campus.chat.repository;

import bingol.campus.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant,Long> {
}
