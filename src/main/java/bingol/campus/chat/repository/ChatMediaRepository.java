package bingol.campus.chat.repository;

import bingol.campus.chat.entity.ChatMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMediaRepository extends JpaRepository<ChatMedia, Long> {
}