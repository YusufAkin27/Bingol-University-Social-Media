package bingol.campus.admin;

import bingol.campus.security.repository.UserRepository;
import bingol.campus.student.business.abstracts.StudentService;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean definitions for {@link AdminController}.
 */
@Generated
public class AdminController__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'adminController'.
   */
  private static BeanInstanceSupplier<AdminController> getAdminControllerInstanceSupplier() {
    return BeanInstanceSupplier.<AdminController>forConstructor(StudentService.class, UserRepository.class, PasswordEncoder.class)
            .withGenerator((registeredBean, args) -> new AdminController(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'adminController'.
   */
  public static BeanDefinition getAdminControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(AdminController.class);
    beanDefinition.setInstanceSupplier(getAdminControllerInstanceSupplier());
    return beanDefinition;
  }
}
