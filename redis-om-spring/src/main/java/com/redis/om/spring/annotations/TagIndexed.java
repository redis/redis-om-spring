package com.redis.om.spring.annotations;

import java.lang.annotation.*;

@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface TagIndexed {
  String fieldName() default "";

  String alias() default "";

  boolean noindex() default false;

  String separator() default "|";

  // Implement official null support - https://github.com/redis/redis-om-spring/issues/527
  boolean indexMissing() default false;

  boolean indexEmpty() default false;

}