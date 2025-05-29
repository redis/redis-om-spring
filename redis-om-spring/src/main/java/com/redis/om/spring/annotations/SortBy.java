package com.redis.om.spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.domain.Sort.Direction;

/**
 * Annotation for defining sort parameters on repository query methods.
 * <p>
 * This annotation allows developers to specify sorting criteria for query results,
 * including the field to sort by and the sort direction. It can be applied to
 * repository methods to control the ordering of returned results.
 * </p>
 * <p>
 * When multiple {@code @SortBy} annotations are present (using a container annotation),
 * they are applied in the order they are declared.
 * </p>
 *
 * <h2>Example usage:</h2>
 * <pre>{@code
 * @Query("*")
 * 
 * @SortBy(field = "lastName", direction = Direction.ASC)
 *               List<Person> findAllSortedByLastName();
 * 
 *               @Query("age:[18 TO *]")
 * @SortBy(field = "age", direction = Direction.DESC)
 *               List<Person> findAdultsSortedByAgeDesc();
 *               }</pre>
 *
 * @see org.springframework.data.domain.Sort.Direction
 * @see Query
 * @since 0.1.0
 */
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface SortBy {
  /**
   * The field name to sort by.
   * <p>
   * This should match the field name as it appears in the entity class or
   * the search alias if a custom alias has been defined. The field must be
   * sortable in the RediSearch index.
   * </p>
   *
   * @return the field name for sorting
   */
  String field();

  /**
   * The direction to sort in.
   * <p>
   * Specifies whether to sort in ascending ({@link Direction#ASC}) or
   * descending ({@link Direction#DESC}) order. Defaults to ascending order.
   * </p>
   *
   * @return the sort direction
   */
  Direction direction() default Direction.ASC;
}
