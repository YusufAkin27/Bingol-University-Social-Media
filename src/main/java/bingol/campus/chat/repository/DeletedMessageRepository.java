package bingol.campus.chat.repository;

import bingol.campus.chat.entity.DeletedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedMessageRepository extends JpaRepository<DeletedMessage, Long> {
}