package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark fields as searchable in RediSearch indexes.
 * This annotation provides full-text search capabilities for text fields
 * with various configuration options for search behavior.
 * 
 * <p>Fields annotated with {@code @Searchable} will be indexed as TEXT fields
 * in RediSearch, enabling full-text search operations. This is typically used
 * on String fields that contain text content that should be searchable.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Searchable(weight = 2.0, sortable = true)
 * private String title;
 * 
 * @Searchable(alias = "desc", nostem = true)
 *                   private String description;
 *                   }
 *                   </pre>
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
public @interface Searchable {
  /**
   * The name of the field in the RediSearch index.
   * If not specified, the Java field name will be used.
   * 
   * @return the field name for indexing
   */
  String fieldName() default "";

  /**
   * An alias for the field that can be used in queries.
   * Provides an alternative name for referencing the field in search operations.
   * 
   * @return the field alias
   */
  String alias() default "";

  /**
   * Whether the field should be sortable in search results.
   * Sortable fields can be used in ORDER BY clauses but require additional memory.
   * 
   * @return true if the field should be sortable, false otherwise
   */
  boolean sortable() default false;

  /**
   * Whether the field should be excluded from indexing.
   * When true, the field is stored but not indexed for search.
   * 
   * @return true if the field should not be indexed, false otherwise
   */
  boolean noindex() default false;

  /**
   * The weight of the field in search relevance scoring.
   * Higher weights make matches in this field more important for ranking.
   * 
   * @return the field weight (default is 1.0)
   */
  double weight() default 1.0;

  /**
   * Whether stemming should be disabled for this field.
   * When true, prevents reduction of words to their root form during indexing.
   * 
   * @return true if stemming should be disabled, false otherwise
   */
  boolean nostem() default false;

  /**
   * The phonetic matching algorithm to use for this field.
   * Enables fuzzy matching based on phonetic similarity (e.g., "dm:en" for Metaphone).
   * 
   * @return the phonetic algorithm identifier
   */
  String phonetic() default "";

  /**
   * Whether to index missing (null) values for this field.
   * When enabled, allows searching for documents where this field is null.
   * 
   * @return true if missing values should be indexed, false otherwise
   */
  // Implement official null support - https://github.com/redis/redis-om-spring/issues/527
  boolean indexMissing() default false;

  /**
   * Whether to index empty string values for this field.
   * When enabled, allows searching for documents where this field is an empty string.
   * 
   * @return true if empty values should be indexed, false otherwise
   */
  boolean indexEmpty() default false;
}
