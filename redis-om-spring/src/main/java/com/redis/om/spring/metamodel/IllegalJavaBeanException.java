package com.redis.om.spring.metamodel;

import java.io.Serial;

/**
 * Exception thrown when a field cannot be matched to a JavaBean getter.
 */
public class IllegalJavaBeanException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 92362727623423324L;

  /**
   * Constructs a new IllegalJavaBeanException with a detailed message.
   *
   * @param clazz     the class containing the problematic field
   * @param fieldName the name of the field that could not be matched
   */
  public IllegalJavaBeanException(final Class<?> clazz, final String fieldName) {
    super(String.format(
        "The field '%s.%s' could not be matched to any getter. Please update your %s class to reflect standard JavaBean notation.",
        clazz.getName(), fieldName, clazz.getName()));
  }

}
