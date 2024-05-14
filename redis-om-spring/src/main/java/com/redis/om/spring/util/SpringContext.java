package com.redis.om.spring.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {

  private static ApplicationContext context;

  /**
   * Returns the Spring managed bean instance of the given class type if it exists.
   * Returns null otherwise.
   *
   * @param beanClass
   * @return
   */
  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

  /**
   * Private method context setting (better practice for setting a static field in a bean
   * instance - see comments of this article for more info).
   */
  private static synchronized void setContext(ApplicationContext context) {
    SpringContext.context = context;
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    // store ApplicationContext reference to access required beans later on
    setContext(context);
  }
}
