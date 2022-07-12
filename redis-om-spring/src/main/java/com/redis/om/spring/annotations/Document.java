package com.redis.om.spring.annotations;

import java.lang.annotation.ElementType;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.annotation.KeySpace;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@KeySpace
public @interface Document {

  @AliasFor(annotation = KeySpace.class, attribute = "value")
  String value() default "";
  
  boolean async() default false;
  String[] prefixes() default {};
  String filter() default "";
  String languageField() default "";
  String language() default "";
  double score() default 1.0; 
  
  /**
   * Time before expire in seconds. Superseded by {@link TimeToLive}.
   *
   * @return positive number when expiration should be applied.
   */
  long timeToLive() default -1L;
}
