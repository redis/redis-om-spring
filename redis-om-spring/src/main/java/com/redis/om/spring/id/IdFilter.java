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
  Class<? extends IdentifierFilter<?>> value();
}
