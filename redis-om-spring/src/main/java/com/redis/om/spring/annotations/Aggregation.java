package com.redis.om.spring.annotations;

import java.lang.annotation.*;

@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
public @interface Aggregation {
  String value() default "*";

  boolean verbatim() default false;

  Load[] load() default {};

  long timeout() default Long.MIN_VALUE;

  Apply[] apply() default {};

  int limit() default Integer.MIN_VALUE;

  int offset() default Integer.MIN_VALUE;

  String[] filter() default {};

  GroupBy[] groupBy() default {};

  SortBy[] sortBy() default {};

  int sortByMax() default Integer.MIN_VALUE;
}
