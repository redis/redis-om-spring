package com.redis.om.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface Indexed {
  String fieldName() default "";

  String alias() default "";

  boolean sortable() default false;

  boolean noindex() default false;

  double weight() default 1.0;

  boolean nostem() default false;

  String phonetic() default "";
  
  String separator() default "";
  
  int arrayIndex() default Integer.MIN_VALUE;
}
