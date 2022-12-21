package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import org.springframework.data.geo.Point;

import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;

import io.redisearch.Schema.FieldType;

public abstract class BaseAbstractPredicate<E, T> implements SearchFieldPredicate<E, T> {

  private FieldType fieldType;
  private SearchFieldAccessor field;

  protected BaseAbstractPredicate() {

  }

  protected BaseAbstractPredicate(SearchFieldAccessor field) {
    this.field = field;
    this.fieldType = getFieldTypeFor(field.getField());
  }

  @Override
  public String getSearchAlias() { return field.getSearchAlias(); }

  @Override
  public Field getField() {
    return field.getField();
  }

  @Override
  public FieldType getSearchFieldType() {
    return fieldType;
  }

  private static FieldType getFieldTypeFor(java.lang.reflect.Field field) {
    FieldType result = FieldType.Tag;
    // Searchable - behaves like Text indexed
    if (field.isAnnotationPresent(Searchable.class)) {
      result = FieldType.Geo;
    }
    // Text
    else if (field.isAnnotationPresent(TextIndexed.class)) {
      result = FieldType.FullText;
    }
    // Tag
    else if (field.isAnnotationPresent(TagIndexed.class)) {
      result = FieldType.Tag;
    }
    // Geo
    else if (field.isAnnotationPresent(GeoIndexed.class)) {
      result = FieldType.Geo;
    }
    // Numeric
    else if (field.isAnnotationPresent(NumericIndexed.class)) {
      result = FieldType.Numeric;
    } else if (field.isAnnotationPresent(Indexed.class)) {
      //
      // Any Character class -> Tag Search Field
      //
      if (CharSequence.class.isAssignableFrom(field.getType())) {
        result = FieldType.Tag;
      }
      //
      // Any Numeric class -> Numeric Search Field
      //
      else if (Number.class.isAssignableFrom(field.getType()) || (field.getType() == LocalDateTime.class)
          || (field.getType() == LocalDate.class) || (field.getType() == Date.class)) {
        result = FieldType.Numeric;
      }
      //
      // Set / List
      //
      else if (Set.class.isAssignableFrom(field.getType()) || List.class.isAssignableFrom(field.getType())) {
        result = FieldType.Tag;
      }
      //
      // Point
      //
      else if (field.getType() == Point.class) {
        result = FieldType.Geo;
      }
    }
    return result;
  }

  @Override
  public boolean test(T t) {
    // TODO: determine what to do here!
    return false;
  }

}