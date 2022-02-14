package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;
import java.util.List;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<T> values;

  public InPredicate(Field field, List<T> values) {
    super(field);
    this.values = values;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.IN;
  }

  @Override
  public boolean test(T t) {
    return this.equals(t);
  }
  
  public List<T> getValues() {
    return values;
  }

}
