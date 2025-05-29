package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a field for text indexing in RediSearch.
 * 
 * <p>This annotation enables full-text search capabilities on String fields,
 * allowing for advanced text search features including:</p>
 * <ul>
 * <li>Full-text search with stemming</li>
 * <li>Phonetic matching</li>
 * <li>Weighted search results</li>
 * <li>Sortable text fields</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Document
 * public class Article {
 * 
 * @TextIndexed(sortable = true, weight = 2.0)
 *                       private String title;
 * 
 * @TextIndexed(nostem = true)
 *                     private String content;
 *                     }
 *                     }</pre>
 *
 * @author Redis OM Spring Developers
 * @see Indexed
 * @see Searchable
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface TextIndexed {
  /**
   * Custom field name to use in the index.
   * 
   * <p>If not specified, the actual field name will be used.</p>
   * 
   * @return the custom field name, defaults to empty string (use field name)
   */
  String fieldName() default "";

  /**
   * Alias for the field in search queries.
   * 
   * <p>Allows using an alternative name when querying this field.</p>
   * 
   * @return the field alias, defaults to empty string (no alias)
   */
  String alias() default "";

  /**
   * Whether this field should be sortable.
   * 
   * <p>When true, allows sorting search results by this field.
   * Note that sortable fields consume more memory.</p>
   * 
   * @return true if the field should be sortable, defaults to false
   */
  boolean sortable() default false;

  /**
   * Whether to skip indexing this field.
   * 
   * <p>When true, the field will be stored but not indexed for search.
   * Useful for fields that should be retrievable but not searchable.</p>
   * 
   * @return true if the field should not be indexed, defaults to false
   */
  boolean noindex() default false;

  /**
   * Weight multiplier for this field in search scoring.
   * 
   * <p>Higher weights increase the importance of matches in this field.
   * The default weight is 1.0. For example, a weight of 2.0 makes matches
   * in this field twice as important for scoring.</p>
   * 
   * @return the field weight, must be positive, defaults to 1.0
   */
  double weight() default 1.0;

  /**
   * Whether to disable stemming for this field.
   * 
   * <p>When true, disables linguistic stemming. Useful for fields containing
   * proper nouns, technical terms, or codes where stemming would be inappropriate.</p>
   * 
   * @return true if stemming should be disabled, defaults to false
   */
  boolean nostem() default false;

  /**
   * Phonetic algorithm to use for this field.
   * 
   * <p>Enables phonetic matching using algorithms like Double Metaphone.
   * Supported values depend on RediSearch configuration.</p>
   * 
   * @return the phonetic algorithm name, defaults to empty string (no phonetic matching)
   */
  String phonetic() default "";

  /**
   * Whether to index documents where this field is missing.
   * 
   * <p>When true, documents with null values for this field will still be indexed.
   * This is part of official null support implementation.</p>
   * 
   * @return true if missing fields should be indexed, defaults to false
   * @see <a href="https://github.com/redis/redis-om-spring/issues/527">Issue #527</a>
   */
  // Implement official null support - https://github.com/redis/redis-om-spring/issues/527
  boolean indexMissing() default false;

  /**
   * Whether to index empty string values.
   * 
   * <p>When true, empty strings will be indexed as searchable values.
   * By default, empty strings are not indexed.</p>
   * 
   * @return true if empty values should be indexed, defaults to false
   */
  boolean indexEmpty() default false;

}