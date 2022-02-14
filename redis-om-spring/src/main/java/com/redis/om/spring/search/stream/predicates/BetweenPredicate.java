package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;

public class BetweenPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private T min;
  private T max;

  public BetweenPredicate(Field field, T min, T max) {
    super(field);
    this.min = min;
    this.max = max;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.BETWEEN;
  }

  @Override
  public boolean test(T t) {
    return this.equals(t);
  }
  
  public T getMin() {
    return min;
  }
  
  public T getMax() {
    return max;
  }


}
