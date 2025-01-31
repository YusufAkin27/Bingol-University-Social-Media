package bingol.campus.comment.repository;

import bingol.campus.comment.entity.Comment;
import bingol.campus.post.entity.Post;
import bingol.campus.story.entity.Story;
import bingol.campus.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    Page<Comment> findByPost(Post post, Pageable pageable);

    Page<Comment> findByStory(Story story, Pageable pageRequest);

    Page<Comment> findByStudent(Student student, Pageable pageable);
}
