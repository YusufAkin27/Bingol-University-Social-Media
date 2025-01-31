package bingol.campus.post.repository;

import bingol.campus.post.entity.Post;
import bingol.campus.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long>{

    Page<Post> findByStudentInAndIsActiveTrueAndIsDeleteFalse(List<Student> followingList, Pageable pageable);

    List<Post> findByStudent(Student student1);

    Page<Post> findByStudentAndIsActive(Student student, boolean b, Pageable pageable);

}
