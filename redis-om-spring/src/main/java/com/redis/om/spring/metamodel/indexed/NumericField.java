package com.redis.om.spring.metamodel.indexed;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.NumIncrByAction;
import com.redis.om.spring.search.stream.predicates.numeric.*;

/**
 * Represents a numeric field in the Redis OM Spring metamodel that supports
 * numeric search operations and predicates. This class provides methods for
 * creating various numeric comparison predicates such as equality, inequality,
 * range queries, and numeric operations like increment/decrement.
 * 
 * <p>Numeric fields are typically used with RediSearch to enable efficient
 * numeric indexing and querying capabilities on Redis documents and hashes.
 * 
 * @param <E> the entity type that contains this numeric field
 * @param <T> the numeric type of the field (e.g., Integer, Long, Double, Float)
 * 
 * @see com.redis.om.spring.annotations.NumericIndexed
 * @see com.redis.om.spring.metamodel.MetamodelField
 * @author Redis OM Spring Team
 */
public class NumericField<E, T> extends MetamodelField<E, T> {

  /**
   * Constructs a NumericField with the specified field accessor and indexing status.
   * 
   * @param field   the search field accessor for this numeric field
   * @param indexed whether this field is indexed for search operations
   */
  public NumericField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Constructs a NumericField for the specified target class and field name.
   * 
   * @param targetClass the class containing this numeric field
   * @param fieldName   the name of the numeric field
   */
  public NumericField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates an equality predicate for this numeric field.
   * 
   * @param value the value to compare for equality
   * @return an EqualPredicate that matches entities where this field equals the specified value
   */
  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-equal predicate for this numeric field.
   * 
   * @param value the value to compare for inequality
   * @return a NotEqualPredicate that matches entities where this field does not equal the specified value
   */
  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a greater-than predicate for this numeric field.
   * 
   * @param value the value to compare against
   * @return a GreaterThanPredicate that matches entities where this field is greater than the specified value
   */
  public GreaterThanPredicate<E, T> gt(T value) {
    return new GreaterThanPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a greater-than-or-equal predicate for this numeric field.
   * 
   * @param value the value to compare against
   * @return a GreaterThanOrEqualPredicate that matches entities where this field is greater than or equal to the
   *         specified value
   */
  public GreaterThanOrEqualPredicate<E, T> ge(T value) {
    return new GreaterThanOrEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a less-than predicate for this numeric field.
   * 
   * @param value the value to compare against
   * @return a LessThanPredicate that matches entities where this field is less than the specified value
   */
  public LessThanPredicate<E, T> lt(T value) {
    return new LessThanPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a less-than-or-equal predicate for this numeric field.
   * 
   * @param value the value to compare against
   * @return a LessThanOrEqualPredicate that matches entities where this field is less than or equal to the specified
   *         value
   */
  public LessThanOrEqualPredicate<E, T> le(T value) {
    return new LessThanOrEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a between predicate for this numeric field to match values within a range.
   * 
   * @param min the minimum value (inclusive)
   * @param max the maximum value (inclusive)
   * @return a BetweenPredicate that matches entities where this field is between min and max (inclusive)
   */
  public BetweenPredicate<E, T> between(T min, T max) {
    return new BetweenPredicate<>(searchFieldAccessor, min, max);
  }

  /**
   * Creates an in predicate for this numeric field to match a single value.
   * 
   * @param value the value to match
   * @return an InPredicate that matches entities where this field equals the specified value
   */
  public InPredicate<E, ?> in(T value) {
    return new InPredicate<>(searchFieldAccessor, List.of(value));
  }

  /**
   * Creates an in predicate for this numeric field to match any of the specified values.
   * 
   * @param values the values to match against
   * @return an InPredicate that matches entities where this field equals any of the specified values
   */
  @SuppressWarnings(
    "unchecked"
  )
  public InPredicate<E, ?> in(T... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  /**
   * Creates an increment action for this numeric field.
   * 
   * @param value the amount to increment by
   * @return a Consumer that increments this field by the specified value
   */
  public Consumer<E> incrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, value);
  }

  /**
   * Creates a decrement action for this numeric field.
   * 
   * @param value the amount to decrement by
   * @return a Consumer that decrements this field by the specified value
   */
  public Consumer<E> decrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, -value);
  }

}
