package com.redis.om.spring.annotations;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Reducer {
  ReducerFunction func();
  String[] args() default {};
  String alias() default "";
}
