package bingol.campus.security.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link JwtService}.
 */
@Generated
public class JwtService__BeanDefinitions {
  /**
   * Get the bean definition for 'jwtService'.
   */
  public static BeanDefinition getJwtServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(JwtService.class);
    InstanceSupplier<JwtService> instanceSupplier = InstanceSupplier.using(JwtService::new);
    instanceSupplier = instanceSupplier.andThen(JwtService__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
