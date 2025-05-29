package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.numeric.*;

/**
 * Metamodel field representing a date field in a Redis OM Spring entity.
 * <p>
 * This class provides type-safe query methods for date-based fields, offering
 * semantic date comparison operations in addition to standard numeric comparisons.
 * Date fields are indexed as numeric values in Redis Search, enabling efficient
 * temporal queries.
 * </p>
 * <p>
 * Date fields support various Java temporal types:
 * <ul>
 * <li>{@link java.util.Date}</li>
 * <li>{@link java.time.LocalDate}</li>
 * <li>{@link java.time.LocalDateTime}</li>
 * <li>{@link java.time.OffsetDateTime}</li>
 * <li>Other temporal types supported by Redis OM Spring converters</li>
 * </ul>
 * <p>
 * Example usage in entity streams:
 * <pre>{@code
 * // Query for events after a specific date
 * entityStream.filter(Event$.eventDate.after(LocalDate.of(2023, 1, 1)))
 * 
 * // Query for events in a date range
 * entityStream.filter(Event$.eventDate.between(startDate, endDate))
 * 
 * // Query for events on or before today
 * entityStream.filter(Event$.eventDate.onOrBefore(LocalDate.now()))
 * }</pre>
 *
 * @param <E> the entity type containing this field
 * @param <T> the date type of this field
 * @see MetamodelField
 * @see SearchFieldAccessor
 * @since 0.1.0
 */
public class DateField<E, T> extends MetamodelField<E, T> {

  /**
   * Creates a new DateField with the specified search field accessor.
   * <p>
   * This constructor is typically used by the metamodel generation process
   * when creating field instances with pre-configured accessors.
   * </p>
   *
   * @param field   the search field accessor for this date field
   * @param indexed whether this field is indexed for search operations
   */
  public DateField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates a new DateField for the specified entity class and field name.
   * <p>
   * This constructor is used when creating field instances with basic class
   * and field name information, typically in static metamodel initialization.
   * </p>
   *
   * @param targetClass the entity class containing this field
   * @param fieldName   the name of the field in the entity class
   */
  public DateField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates a predicate that matches dates equal to the specified value.
   *
   * @param value the date value to match
   * @return a predicate for exact date matching
   */
  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a predicate that matches dates not equal to the specified value.
   *
   * @param value the date value to exclude
   * @return a predicate for non-matching dates
   */
  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a predicate that matches dates after the specified value.
   * This is equivalent to a "greater than" comparison for dates.
   *
   * @param value the reference date (exclusive)
   * @return a predicate for dates after the specified value
   */
  public GreaterThanPredicate<E, T> after(T value) {
    return new GreaterThanPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a predicate that matches dates on or after the specified value.
   * This is equivalent to a "greater than or equal" comparison for dates.
   * 
   * @param value the minimum date (inclusive)
   * @return a predicate for dates on or after the specified value
   */
  public GreaterThanOrEqualPredicate<E, T> onOrAfter(T value) {
    return new GreaterThanOrEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a predicate that matches dates before the specified value.
   * This is equivalent to a "less than" comparison for dates.
   *
   * @param value the reference date (exclusive)
   * @return a predicate for dates before the specified value
   */
  public LessThanPredicate<E, T> before(T value) {
    return new LessThanPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a predicate that matches dates on or before the specified value.
   * This is equivalent to a "less than or equal" comparison for dates.
   * 
   * @param value the maximum date (inclusive)
   * @return a predicate for dates on or before the specified value
   */
  public LessThanOrEqualPredicate<E, T> onOrBefore(T value) {
    return new LessThanOrEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a predicate that matches dates within the specified range (inclusive).
   * This is equivalent to a "greater than or equal AND less than or equal" comparison.
   *
   * @param min the minimum date (inclusive)
   * @param max the maximum date (inclusive)
   * @return a predicate for dates within the specified range
   */
  public BetweenPredicate<E, T> between(T min, T max) {
    return new BetweenPredicate<>(searchFieldAccessor, min, max);
  }

}
