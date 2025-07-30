package com.redis.om.spring.metamodel.indexed;

import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;
import com.redis.om.spring.search.stream.predicates.lexicographic.LexicographicBetweenMarker;
import com.redis.om.spring.search.stream.predicates.lexicographic.LexicographicGreaterThanMarker;
import com.redis.om.spring.search.stream.predicates.lexicographic.LexicographicLessThanMarker;
import com.redis.om.spring.search.stream.predicates.tag.EndsWithPredicate;
import com.redis.om.spring.search.stream.predicates.tag.StartsWithPredicate;

/**
 * Represents a metamodel field that combines both text and tag indexing capabilities.
 * This specialized field type allows for hybrid search functionality, supporting both
 * tag-based exact matching operations and text-based pattern matching operations
 * such as starts-with and ends-with queries.
 * 
 * <p>Text-tag fields are particularly useful when you need to perform both exact
 * tag matching and partial string matching on the same field. Common use cases include:
 * <ul>
 * <li>Username or email fields where you need both exact matches and prefix searches</li>
 * <li>Product codes that require both exact matching and pattern-based queries</li>
 * <li>Category hierarchies that need both exact category matching and path-based searches</li>
 * </ul>
 * 
 * <p>This field type extends {@link TagField} and adds text-specific operations while
 * maintaining all tag-based functionality from the parent class.
 * 
 * @param <E> The entity type containing this field
 * @param <T> The type of the field value
 * @see TagField
 * @see StartsWithPredicate
 * @see EndsWithPredicate
 */
public class TextTagField<E, T> extends TagField<E, T> {

  /**
   * Constructs a TextTagField with specified field accessor and indexing status.
   * 
   * @param field   The search field accessor for this field
   * @param indexed Whether this field is indexed for search operations
   */
  public TextTagField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Constructs a TextTagField for the specified target class and field name.
   * This constructor uses reflection to create the appropriate field accessor.
   * 
   * @param targetClass The class containing the field
   * @param fieldName   The name of the field in the target class
   */
  public TextTagField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates a predicate that matches values starting with the specified prefix.
   * This operation performs a prefix match on the field value.
   * 
   * @param value The prefix value to match against
   * @return A predicate that evaluates to true when the field starts with the given value
   */
  public StartsWithPredicate<E, T> startsWith(T value) {
    return new StartsWithPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a predicate that matches values ending with the specified suffix.
   * This operation performs a suffix match on the field value.
   * 
   * @param value The suffix value to match against
   * @return A predicate that evaluates to true when the field ends with the given value
   */
  public EndsWithPredicate<E, T> endsWith(T value) {
    return new EndsWithPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a consumer that appends a string value to this field.
   * This operation modifies the field value by concatenating the specified string.
   * 
   * @param value The string value to append to the field
   * @return A consumer that performs the append operation on an entity
   */
  public Consumer<E> append(String value) {
    return new StringAppendAction<>(searchFieldAccessor, value);
  }

  /**
   * Returns a function that calculates the string length of this field's value.
   * 
   * @return A function that returns the length of the field's string representation
   */
  @Override
  public ToLongFunction<E> length() {
    return new StrLengthAction<>(searchFieldAccessor);
  }

  /**
   * Creates a lexicographic greater-than predicate for this text-tag field.
   * <p>
   * This predicate requires the field to be indexed with {@code lexicographic=true}
   * in its {@code @Indexed} annotation. It matches fields that are
   * lexicographically greater than the specified value.
   * </p>
   *
   * @param value the value to compare against
   * @return a LexicographicGreaterThanMarker that matches entities where this field is lexicographically greater than
   *         the specified value
   * @since 1.0
   */
  public LexicographicGreaterThanMarker<E, T> gt(T value) {
    return new LexicographicGreaterThanMarker<>(searchFieldAccessor, value);
  }

  /**
   * Creates a lexicographic less-than predicate for this text-tag field.
   * <p>
   * This predicate requires the field to be indexed with {@code lexicographic=true}
   * in its {@code @Indexed} annotation. It matches fields that are
   * lexicographically less than the specified value.
   * </p>
   *
   * @param value the value to compare against
   * @return a LexicographicLessThanMarker that matches entities where this field is lexicographically less than the
   *         specified value
   * @since 1.0
   */
  public LexicographicLessThanMarker<E, T> lt(T value) {
    return new LexicographicLessThanMarker<>(searchFieldAccessor, value);
  }

  /**
   * Creates a lexicographic between predicate for this text-tag field.
   * <p>
   * This predicate requires the field to be indexed with {@code lexicographic=true}
   * in its {@code @Indexed} annotation. It matches fields that are
   * lexicographically between the specified min and max values (inclusive).
   * </p>
   *
   * @param min the minimum value (inclusive)
   * @param max the maximum value (inclusive)
   * @return a LexicographicBetweenMarker that matches entities where this field is between min and max
   * @since 1.0
   */
  public LexicographicBetweenMarker<E, T> between(T min, T max) {
    return new LexicographicBetweenMarker<>(searchFieldAccessor, min, max);
  }
}
