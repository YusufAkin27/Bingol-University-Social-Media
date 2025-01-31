package bingol.campus.story.repository;

import bingol.campus.story.entity.StoryViewer;
import bingol.campus.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryViewerRepository extends JpaRepository<StoryViewer,Long> {
    List<Long> findViewedStoryIdsByStudent(Student student);

}
