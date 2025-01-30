package bingol.campus.story.business.concretes;

import bingol.campus.comment.core.converter.CommentConverter;
import bingol.campus.comment.repository.CommentRepository;
import bingol.campus.like.core.converter.LikeConverter;
import bingol.campus.story.core.converter.StoryConverter;
import bingol.campus.story.repository.FeaturedStoryRepository;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.story.repository.StoryViewerRepository;
import bingol.campus.student.core.converter.StudentConverter;
import bingol.campus.student.repository.StudentRepository;
import com.cloudinary.Cloudinary;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link StoryManager}.
 */
@Generated
public class StoryManager__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'storyManager'.
   */
  private static BeanInstanceSupplier<StoryManager> getStoryManagerInstanceSupplier() {
    return BeanInstanceSupplier.<StoryManager>forConstructor(StudentRepository.class, StoryRepository.class, Cloudinary.class, StoryConverter.class, StudentConverter.class, FeaturedStoryRepository.class, CommentConverter.class, StoryViewerRepository.class, LikeConverter.class, CommentRepository.class)
            .withGenerator((registeredBean, args) -> new StoryManager(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6), args.get(7), args.get(8), args.get(9)));
  }

  /**
   * Get the bean definition for 'storyManager'.
   */
  public static BeanDefinition getStoryManagerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(StoryManager.class);
    beanDefinition.setInstanceSupplier(getStoryManagerInstanceSupplier());
    return beanDefinition;
  }
}
