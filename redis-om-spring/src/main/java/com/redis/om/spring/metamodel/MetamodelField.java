package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.function.Function;

public class MetamodelField<E, T> implements Comparator<E>, Function<E,T> {

  protected final Field field;
  protected final boolean indexed;
  
  public MetamodelField(Field field, boolean indexed) {
    this.field = field;
    this.indexed = indexed;
  }
  
  public Field getField() {
    return field;
  }

  @Override
  public int compare(E o1, E o2) {
    return 0;
  }

  @Override
  public T apply(E t) {
    return null;
  }
  
  public boolean isIndexed() {
    return indexed;
  }
}
