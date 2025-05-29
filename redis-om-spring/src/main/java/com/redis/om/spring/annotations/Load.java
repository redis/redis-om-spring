package com.redis.om.spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify loading behavior for entity properties in Redis OM Spring.
 * This annotation can be applied to methods and other annotations to control how
 * related data is loaded and accessed.
 */
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface Load {
  /**
   * Specifies the property name to load.
   * If not specified, the property name will be derived from the method name.
   *
   * @return the property name to load
   */
  String property() default "";

  /**
   * Specifies an alias for the loaded property.
   * This can be used to provide an alternative name for the property
   * in the loading context.
   *
   * @return the alias for the property
   */
  String alias() default "";
}
