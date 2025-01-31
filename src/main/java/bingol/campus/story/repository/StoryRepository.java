package bingol.campus.story.repository;

import bingol.campus.story.entity.Story;
import bingol.campus.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story,Long> {
    Page<Story> findByStudentInAndIsActiveTrueOrderByCreatedAtDesc(List<Student> followingList, Pageable pageable);

    List<Story> findByStudent(Student student1);

    Page<Story> findByStudentAndIsActive(Student student, boolean b, Pageable pageable);
}
