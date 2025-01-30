package bingol.campus.post.business.concretes;

import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.comment.repository.CommentRepository;
import bingol.campus.like.core.converter.LikeConverter;
import bingol.campus.like.repository.LikeRepository;
import bingol.campus.post.core.converter.PostConverter;
import bingol.campus.post.repository.PostRepository;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.student.repository.StudentRepository;
import com.cloudinary.Cloudinary;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link PostManager}.
 */
@Generated
public class PostManager__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'postManager'.
   */
  private static BeanInstanceSupplier<PostManager> getPostManagerInstanceSupplier() {
    return BeanInstanceSupplier.<PostManager>forConstructor(PostConverter.class, PostRepository.class, StudentRepository.class, CommentConverter.class, StoryRepository.class, LikeConverter.class, LikeRepository.class, CommentRepository.class, Cloudinary.class)
            .withGenerator((registeredBean, args) -> new PostManager(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6), args.get(7), args.get(8)));
  }

  /**
   * Get the bean definition for 'postManager'.
   */
  public static BeanDefinition getPostManagerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PostManager.class);
    beanDefinition.setInstanceSupplier(getPostManagerInstanceSupplier());
    return beanDefinition;
  }
}
