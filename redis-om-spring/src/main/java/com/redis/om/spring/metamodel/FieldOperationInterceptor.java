package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.function.Function;

public class FieldOperationInterceptor<E, T> implements Comparator<E>, Function<E,T> {

  protected final Field field;
  protected final boolean indexed;
  
  public FieldOperationInterceptor(Field field, boolean indexed) {
    this.field = field;
    this.indexed = indexed;
  }
  
  public Field getField() {
    return field;
  }

  @Override
  public int compare(E o1, E o2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public T apply(E t) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public boolean isIndexed() {
    return indexed;
  }
}
