package com.redis.om.spring.annotations;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.annotation.KeySpace;

import com.redis.om.spring.repository.query.SearchLanguage;

@Persistent
@Inherited
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.TYPE }
)
@KeySpace
public @interface Document {

  @AliasFor(
      annotation = KeySpace.class, attribute = "value"
  )
  String value() default "";

  String indexName() default "";

  boolean async() default false;

  String[] prefixes() default {};

  String filter() default "";

  String languageField() default "";

  SearchLanguage language() default SearchLanguage.ENGLISH;

  double score() default 1.0;

  /**
   * Time before expire in seconds.
   *
   * @return positive number when expiration should be applied.
   */
  long timeToLive() default -1L;
}
