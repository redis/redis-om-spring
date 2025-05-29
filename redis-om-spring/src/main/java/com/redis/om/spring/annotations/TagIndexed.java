package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Marks a field for indexing as a TAG field in RediSearch.
 * <p>
 * TAG fields are designed for exact-match queries and categorical data. Unlike TEXT fields,
 * TAG fields do not undergo tokenization or stemming, making them ideal for:
 * <ul>
 * <li>Categorical values (e.g., status, category, type)</li>
 * <li>Identifiers (e.g., user IDs, product codes)</li>
 * <li>Enumerated values</li>
 * <li>Multi-value fields (using separators)</li>
 * </ul>
 * <p>
 * Key differences from TEXT fields:
 * <ul>
 * <li>No tokenization - values are indexed as-is</li>
 * <li>Case-sensitive matching by default</li>
 * <li>Supports multi-value fields with custom separators</li>
 * <li>More efficient for exact matching operations</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class Product {
 *     // Single-value tag field
 * @TagIndexed
 * private String category;
 * 
 * // Multi-value tag field with comma separator
 * 
 * @TagIndexed(separator = ",")
 *                       private String tags;
 * 
 *                       // Tag field with custom alias for queries
 * @TagIndexed(alias = "product_type")
 *                   private String type;
 *                   }
 *                   }</pre>
 *
 * @see TextIndexed
 * @see com.redis.om.spring.metamodel.indexed.TagField
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface TagIndexed {
  /**
   * Override the field name in the RediSearch index.
   * <p>
   * By default, the Java field name is used as the index field name.
   * Use this attribute to specify a different name in the index,
   * which can be useful for maintaining compatibility with existing
   * schemas or following naming conventions.
   * </p>
   *
   * @return the field name to use in the index, or empty string to use the Java field name
   */
  String fieldName() default "";

  /**
   * Alias for the field in search queries.
   * <p>
   * Provides an alternative name that can be used in queries to reference
   * this field. This is useful for creating more user-friendly query syntax
   * or maintaining backward compatibility when field names change.
   * </p>
   *
   * @return the alias name for queries, or empty string for no alias
   */
  String alias() default "";

  /**
   * Whether to exclude this field from indexing.
   * <p>
   * When set to true, the field will not be indexed for search operations
   * but will still be stored in the document. This is useful for fields
   * that need to be retrieved but not searched.
   * </p>
   *
   * @return true to exclude from indexing, false to include (default)
   */
  boolean noindex() default false;

  /**
   * Separator character for multi-value tag fields.
   * <p>
   * When a string field contains multiple values, this separator is used
   * to split the string into individual tags. Each tag is indexed separately,
   * allowing searches to match any of the individual values.
   * </p>
   * <p>
   * For example, with separator "|" and value "red|blue|green", searches
   * for "red", "blue", or "green" will all match the document.
   * </p>
   *
   * @return the separator character (default is "|")
   */
  String separator() default "|";

  /**
   * Whether to index documents where this field is missing (null).
   * <p>
   * When true, documents with null values for this field will be included
   * in the index and can be found using special queries for missing fields.
   * This is part of the null value support feature.
   * </p>
   *
   * @return true to index missing fields, false to skip them (default)
   * @see <a href="https://github.com/redis/redis-om-spring/issues/527">Issue #527</a>
   */
  // Implement official null support - https://github.com/redis/redis-om-spring/issues/527
  boolean indexMissing() default false;

  /**
   * Whether to index documents where this field is empty.
   * <p>
   * When true, documents with empty string values for this field will be
   * included in the index. This allows distinguishing between null values
   * and empty strings in search queries.
   * </p>
   *
   * @return true to index empty fields, false to skip them (default)
   */
  boolean indexEmpty() default false;

}