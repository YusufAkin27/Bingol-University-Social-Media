package bingol.campus.blockRelation.core.converter;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link BlockRelationConverterImpl}.
 */
@Generated
public class BlockRelationConverterImpl__BeanDefinitions {
  /**
   * Get the bean definition for 'blockRelationConverterImpl'.
   */
  public static BeanDefinition getBlockRelationConverterImplBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BlockRelationConverterImpl.class);
    beanDefinition.setInstanceSupplier(BlockRelationConverterImpl::new);
    return beanDefinition;
  }
}
