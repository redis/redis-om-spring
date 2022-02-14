package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;

public class LikePredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T value;

  public LikePredicate(Field field, T value) {
    super(field);
    this.value = value;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.LIKE;
  }

  @Override
  public boolean test(T t) {
    return this.equals(t);
  }
  
  public T getValue() {
    return value;
  }

}
