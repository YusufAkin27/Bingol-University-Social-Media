package bingol.campus.post.business;

import bingol.campus.post.repository.PostRepository;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link PopularityPostService}.
 */
@Generated
public class PopularityPostService__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'popularityPostService'.
   */
  private static BeanInstanceSupplier<PopularityPostService> getPopularityPostServiceInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<PopularityPostService>forConstructor(PostRepository.class)
            .withGenerator((registeredBean, args) -> new PopularityPostService(args.get(0)));
  }

  /**
   * Get the bean definition for 'popularityPostService'.
   */
  public static BeanDefinition getPopularityPostServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PopularityPostService.class);
    beanDefinition.setInstanceSupplier(getPopularityPostServiceInstanceSupplier());
    return beanDefinition;
  }
}
