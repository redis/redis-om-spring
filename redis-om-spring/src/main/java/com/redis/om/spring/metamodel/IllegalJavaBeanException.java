package com.redis.om.spring.metamodel;

import java.io.Serial;

public class IllegalJavaBeanException extends RuntimeException {

  @Serial private static final long serialVersionUID = 92362727623423324L;

  public IllegalJavaBeanException(final Class<?> clazz, final String fieldName) {
    super(String.format(
        "The field '%s.%s' could not be matched to any getter. Please update your %s class to reflect standard JavaBean notation.",
        clazz.getName(), fieldName, clazz.getName()));
  }

}
