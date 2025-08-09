package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.geo.Point;

import com.redis.om.spring.annotations.*;
import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.search.Schema.FieldType;

/**
 * Abstract base class for search predicates that operate on specific fields.
 * This class provides common functionality for predicates that are bound to
 * specific entity fields and need to determine the appropriate Redis field type.
 * 
 * <p>BaseAbstractPredicate analyzes field annotations to determine the correct
 * Redis field type (TEXT, TAG, NUMERIC, GEO) and provides access to field
 * metadata for query construction.</p>
 * 
 * <p>This class serves as the foundation for field-specific predicates in
 * Redis OM search operations, handling the mapping between Java field types
 * and annotations to Redis search field types.</p>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type of the predicate
 * 
 * @since 1.0
 * @see SearchFieldPredicate
 * @see SearchFieldAccessor
 */
public abstract class BaseAbstractPredicate<E, T> implements SearchFieldPredicate<E, T> {

  /** The Redis field type for this predicate's target field */
  private FieldType fieldType;

  /** The field accessor that provides metadata about the target field */
  private SearchFieldAccessor field;

  /**
   * Creates a new BaseAbstractPredicate without a specific field.
   * This constructor is used for compound predicates that don't target a specific field.
   */
  protected BaseAbstractPredicate() {

  }

  /**
   * Creates a new BaseAbstractPredicate for the specified field.
   * This constructor analyzes the field to determine its Redis field type.
   * 
   * @param field the field accessor for the target field
   */
  protected BaseAbstractPredicate(SearchFieldAccessor field) {
    this.field = field;
    this.fieldType = getFieldTypeFor(field.getField());
  }

  /**
   * Creates a new BaseAbstractPredicate for the specified field with explicit field type.
   * This constructor is used for synthetic fields (like Map VALUES) where the field type
   * cannot be determined from annotations.
   * 
   * @param field     the field accessor for the target field
   * @param fieldType the explicit Redis field type for this predicate
   */
  protected BaseAbstractPredicate(SearchFieldAccessor field, FieldType fieldType) {
    this.field = field;
    this.fieldType = fieldType;
  }

  private static FieldType getFieldTypeFor(java.lang.reflect.Field field) {
    FieldType result = null;

    // Handle null fields (synthetic fields like Map VALUES)
    if (field == null) {
      return null; // Will be determined by the field type in constructor
    }

    // Searchable - behaves like Text indexed
    if (field.isAnnotationPresent(Searchable.class)) {
      result = FieldType.GEO;
    }
    // Text
    else if (field.isAnnotationPresent(TextIndexed.class)) {
      result = FieldType.TEXT;
    }
    // Tag
    else if (field.isAnnotationPresent(TagIndexed.class)) {
      result = FieldType.TAG;
    }
    // Geo
    else if (field.isAnnotationPresent(GeoIndexed.class)) {
      result = FieldType.GEO;
    }
    // Numeric
    else if (field.isAnnotationPresent(NumericIndexed.class)) {
      result = FieldType.NUMERIC;
    } else if (field.isAnnotationPresent(Indexed.class)) {
      //
      // Any Character class -> Tag Search Field
      //
      if (CharSequence.class.isAssignableFrom(field.getType())) {
        result = FieldType.TAG;
      }
      //
      // Any Numeric class -> Numeric Search Field
      //
      else if (Number.class.isAssignableFrom(field.getType()) || (field.getType() == LocalDateTime.class) || (field
          .getType() == LocalDate.class) || (field.getType() == Date.class) || (field.getType() == Instant.class)) {
        result = FieldType.NUMERIC;
      }
      //
      // Set / List
      //
      else if (Set.class.isAssignableFrom(field.getType()) || List.class.isAssignableFrom(field.getType())) {
        result = FieldType.TAG;
      }
      //
      // Point
      //
      else if (field.getType() == Point.class) {
        result = FieldType.GEO;
      }
    }
    return result;
  }

  @Override
  public String getSearchAlias() {
    return field.getSearchAlias();
  }

  @Override
  public Field getField() {
    return field.getField();
  }

  @Override
  public FieldType getSearchFieldType() {
    return fieldType;
  }

  @Override
  public boolean test(T t) {
    // TODO: determine what to do here!
    return false;
  }

  /**
   * Returns the search field accessor for this predicate.
   *
   * @return the search field accessor
   */
  public SearchFieldAccessor getSearchFieldAccessor() {
    return field;
  }

}