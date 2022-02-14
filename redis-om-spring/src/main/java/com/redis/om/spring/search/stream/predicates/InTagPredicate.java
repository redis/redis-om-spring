package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;
import java.util.List;

public class InTagPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<String> values;

  public InTagPredicate(Field field, List<String> list) {
    super(field);
    this.values = list;
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.TAG_IN;
  }

  @Override
  public boolean test(T t) {
    return this.equals(t);
  }
  
  public List<String> getValues() {
    return values;
  }

}
