package com.redis.om.spring.annotations;

import java.lang.annotation.*;

@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface Searchable {
  String fieldName() default "";

  String alias() default "";

  boolean sortable() default false;

  boolean noindex() default false;

  double weight() default 1.0;

  boolean nostem() default false;

  String phonetic() default "";

  // Implement official null support - https://github.com/redis/redis-om-spring/issues/527
  boolean indexMissing() default false;

  boolean indexEmpty() default false;
}
