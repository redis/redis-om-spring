package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a numeric field for indexing in Redis OM Spring.
 * 
 * <p>This annotation enables RediSearch indexing on numeric fields, allowing
 * for efficient numeric range queries, equality checks, and sorting operations.
 * When applied to a field, Redis OM Spring will create a numeric index that
 * supports various comparison operations like greater than, less than, between,
 * and exact matches.
 * 
 * <p>Supported numeric types include:
 * <ul>
 * <li>Integer and int</li>
 * <li>Long and long</li>
 * <li>Double and double</li>
 * <li>Float and float</li>
 * <li>BigDecimal</li>
 * <li>BigInteger</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * @Document
 * public class Product {
 * 
 * @Id
 *     private String id;
 * 
 * @NumericIndexed(sortable = true)
 *                          private Double price;
 * 
 * @NumericIndexed(alias = "qty")
 *                       private Integer quantity;
 *                       }
 *                       }</pre>
 * 
 * @see com.redis.om.spring.metamodel.indexed.NumericField
 * @see com.redis.om.spring.annotations.Indexed
 * @author Redis OM Spring Team
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface NumericIndexed {
  /**
   * Specifies a custom field name to use in the Redis index.
   * 
   * <p>If not specified, the actual field name will be used.
   * This is useful when you want the index field name to differ
   * from the Java field name.
   * 
   * @return the custom field name for indexing, or empty string to use the default field name
   */
  String fieldName() default "";

  /**
   * Specifies an alias for this numeric field in search queries.
   * 
   * <p>Aliases provide an alternative name that can be used in search
   * expressions and queries. This is particularly useful for creating
   * shorter or more meaningful names for complex field paths.
   * 
   * @return the alias for this field, or empty string if no alias is defined
   */
  String alias() default "";

  /**
   * Indicates whether this numeric field should be sortable in search results.
   * 
   * <p>When set to true, RediSearch will create additional indexing structures
   * that allow efficient sorting of search results by this field. This enables
   * ORDER BY clauses in search queries but may increase memory usage.
   * 
   * @return true if the field should be sortable, false otherwise
   */
  boolean sortable() default false;

  /**
   * Indicates whether this field should be excluded from indexing.
   * 
   * <p>When set to true, the field will be stored but not indexed, meaning
   * it cannot be used in search predicates but can still be retrieved in
   * search results. This is useful for fields that need to be stored but
   * not searched upon.
   * 
   * @return true if the field should not be indexed, false to index the field
   */
  boolean noindex() default false;
}