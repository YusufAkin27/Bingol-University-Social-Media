package bingol.campus.story.repository;

import bingol.campus.story.entity.FeaturedStory;
import bingol.campus.student.entity.Student;
import com.google.api.gax.rpc.ServerStream;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeaturedStoryRepository extends JpaRepository<FeaturedStory,Long> {
    ServerStream<FeaturedStory> findAllByStudent(Student student);

}
