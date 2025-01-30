package bingol.campus.like.business.concretes;

import bingol.campus.like.repository.LikeRepository;
import bingol.campus.post.core.converter.PostConverter;
import bingol.campus.post.repository.PostRepository;
import bingol.campus.story.core.converter.StoryConverter;
import bingol.campus.story.repository.StoryRepository;
import bingol.campus.student.core.converter.StudentConverter;
import bingol.campus.student.repository.StudentRepository;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link LikeServiceManager}.
 */
@Generated
public class LikeServiceManager__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'likeServiceManager'.
   */
  private static BeanInstanceSupplier<LikeServiceManager> getLikeServiceManagerInstanceSupplier() {
    return BeanInstanceSupplier.<LikeServiceManager>forConstructor(StudentRepository.class, LikeRepository.class, StoryRepository.class, PostRepository.class, PostConverter.class, StoryConverter.class, StudentConverter.class)
            .withGenerator((registeredBean, args) -> new LikeServiceManager(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6)));
  }

  /**
   * Get the bean definition for 'likeServiceManager'.
   */
  public static BeanDefinition getLikeServiceManagerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(LikeServiceManager.class);
    beanDefinition.setInstanceSupplier(getLikeServiceManagerInstanceSupplier());
    return beanDefinition;
  }
}
