package com.redis.om.spring.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface Cuckoo {
  String name() default "";
  int capacity();
  int bucketSize() default 2;
  int maxIterations() default 20;
  int expansion() default 1;
}
