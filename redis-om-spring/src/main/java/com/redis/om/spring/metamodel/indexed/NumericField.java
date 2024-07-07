package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.NumIncrByAction;
import com.redis.om.spring.search.stream.predicates.numeric.*;

import java.util.Arrays;
import java.util.function.Consumer;

public class NumericField<E, T> extends MetamodelField<E, T> {

  public NumericField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public NumericField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  public GreaterThanPredicate<E, T> gt(T value) {
    return new GreaterThanPredicate<>(searchFieldAccessor, value);
  }

  public GreaterThanOrEqualPredicate<E, T> ge(T value) {
    return new GreaterThanOrEqualPredicate<>(searchFieldAccessor, value);
  }

  public LessThanPredicate<E, T> lt(T value) {
    return new LessThanPredicate<>(searchFieldAccessor, value);
  }

  public LessThanOrEqualPredicate<E, T> le(T value) {
    return new LessThanOrEqualPredicate<>(searchFieldAccessor, value);
  }

  public BetweenPredicate<E, T> between(T min, T max) {
    return new BetweenPredicate<>(searchFieldAccessor, min, max);
  }

  @SuppressWarnings("unchecked")
  public InPredicate<E, ?> in(T... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  public Consumer<E> incrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, value);
  }

  public Consumer<E> decrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, -value);
  }

}
