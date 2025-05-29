package com.redis.om.spring.id;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Filters an identifier before reading/saving to Redis.
 *
 * @author Brian Sam-Bodden
 */
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
    value = { FIELD, METHOD, ANNOTATION_TYPE }
)
public @interface IdFilter {
  /**
   * The identifier filter class to use.
   *
   * @return the filter class
   */
  Class<? extends IdentifierFilter<?>> value();
}
