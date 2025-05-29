package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark fields for geospatial indexing in Redis.
 * <p>
 * This annotation enables geospatial search capabilities on fields containing
 * geographic coordinates. Fields annotated with {@code @GeoIndexed} can be
 * searched using geographic queries such as distance-based searches.
 * </p>
 * 
 * @since 1.0.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface GeoIndexed {
  /**
   * Specifies the field name to use in the Redis index.
   * If not specified, the field name from the Java class will be used.
   * 
   * @return the field name for indexing
   */
  String fieldName() default "";

  /**
   * Specifies an alias for the field in search queries.
   * This allows using a different name when constructing search queries
   * than the actual field name in the index.
   * 
   * @return the field alias for search queries
   */
  String alias() default "";

  /**
   * Indicates whether the field should be stored but not indexed.
   * When set to {@code true}, the field value is stored in Redis but
   * cannot be used in search queries.
   * 
   * @return {@code true} if the field should not be indexed, {@code false} otherwise
   */
  boolean noindex() default false;
}