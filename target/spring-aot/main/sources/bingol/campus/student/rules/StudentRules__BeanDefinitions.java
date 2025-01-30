package bingol.campus.student.rules;

import bingol.campus.student.repository.StudentRepository;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link StudentRules}.
 */
@Generated
public class StudentRules__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'studentRules'.
   */
  private static BeanInstanceSupplier<StudentRules> getStudentRulesInstanceSupplier() {
    return BeanInstanceSupplier.<StudentRules>forConstructor(StudentRepository.class)
            .withGenerator((registeredBean, args) -> new StudentRules(args.get(0)));
  }

  /**
   * Get the bean definition for 'studentRules'.
   */
  public static BeanDefinition getStudentRulesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(StudentRules.class);
    beanDefinition.setInstanceSupplier(getStudentRulesInstanceSupplier());
    return beanDefinition;
  }
}
