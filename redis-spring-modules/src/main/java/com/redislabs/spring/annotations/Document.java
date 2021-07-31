package com.redislabs.spring.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE })
public @interface Document {

  @AliasFor("collection")
  String value() default "";

  @AliasFor("value")
  String collection() default "";
}
