package com.redis.om.spring.metamodel;

public class IllegalJavaBeanException extends RuntimeException {

  private static final long serialVersionUID = 92362727623423324L;

  public IllegalJavaBeanException(final Class<?> clazz, final String fieldName) {
    super(String.format(
        "The field '%s.%s' could not be matched to any getter. Please update your %s class to reflect standard JavaBean notation.",
        clazz.getName(), fieldName, clazz.getName()));
  }

}
