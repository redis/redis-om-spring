package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Marker annotation for metamodel generation.
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface Metamodel {
}
