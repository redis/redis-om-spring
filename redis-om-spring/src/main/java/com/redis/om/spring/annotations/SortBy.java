package com.redis.om.spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.domain.Sort.Direction;

@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface SortBy {
  String field();

  Direction direction() default Direction.ASC;
}
