package com.redis.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.redisearch.Schema.FieldType;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface TagIndexed {
  String fieldName() default "";
  String alias() default "";
  FieldType fieldType() default FieldType.FullText;
  boolean sortable() default false;
  boolean noindex() default false;
  String separator() default ",";
}