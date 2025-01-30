package bingol.campus.comment.business.concretes;

import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.comment.repository.CommentRepository;
import bingol.campus.post.repository.PostRepository;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.student.core.converter.StudentConverter;
import bingol.campus.student.repository.StudentRepository;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link CommentManager}.
 */
@Generated
public class CommentManager__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'commentManager'.
   */
  private static BeanInstanceSupplier<CommentManager> getCommentManagerInstanceSupplier() {
    return BeanInstanceSupplier.<CommentManager>forConstructor(StudentRepository.class, StoryRepository.class, PostRepository.class, StudentConverter.class, CommentConverter.class, CommentRepository.class)
            .withGenerator((registeredBean, args) -> new CommentManager(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'commentManager'.
   */
  public static BeanDefinition getCommentManagerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CommentManager.class);
    beanDefinition.setInstanceSupplier(getCommentManagerInstanceSupplier());
    return beanDefinition;
  }
}
