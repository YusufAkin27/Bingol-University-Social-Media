package bingol.campus.chat.repository;

import bingol.campus.chat.entity.PinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, Long> {
}