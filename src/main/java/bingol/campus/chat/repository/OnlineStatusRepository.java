package bingol.campus.chat.repository;

import bingol.campus.chat.entity.OnlineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnlineStatusRepository extends JpaRepository<OnlineStatus, Long> {
    OnlineStatus findByStudentId(Long studentId);
}