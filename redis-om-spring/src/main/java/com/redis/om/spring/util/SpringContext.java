package com.redis.om.spring.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class that provides static access to the Spring ApplicationContext.
 * <p>
 * This class implements {@link ApplicationContextAware} to receive the application context
 * during Spring's initialization phase and stores it in a static field. This allows
 * non-Spring-managed classes to access Spring beans when dependency injection is not available.
 * </p>
 * <p>
 * <strong>Note:</strong> This class should be used sparingly as it creates tight coupling
 * to the Spring container. Prefer constructor or setter injection where possible.
 * </p>
 *
 * <h2>Example usage:</h2>
 * <pre>{@code
 * // Get a Spring-managed bean from a non-Spring class
 * MyService service = SpringContext.getBean(MyService.class);
 * }</pre>
 *
 * @see ApplicationContextAware
 * @since 0.1.0
 */
@Component
public class SpringContext implements ApplicationContextAware {

  /**
   * Default constructor for Spring Context utility.
   * <p>
   * This constructor is used by Spring's component scanning to create
   * the singleton instance of this utility class.
   */
  public SpringContext() {
    // Default constructor for Spring component instantiation
  }

  private static ApplicationContext context;

  /**
   * Returns the Spring managed bean instance of the given class type if it exists.
   * Returns null otherwise.
   *
   * @param <T>       the type of the bean to retrieve
   * @param beanClass the class of the bean to retrieve
   * @return the bean instance, or null if no bean of the specified class exists
   */
  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

  /**
   * Sets the static application context reference in a thread-safe manner.
   * <p>
   * This method is synchronized to ensure thread safety when setting the static field.
   * Using a separate private method for setting static fields from instance methods
   * is a recommended practice to avoid potential issues with concurrent access.
   * </p>
   *
   * @param context the ApplicationContext to store
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
