package com.redis.om.spring.metamodel.nonindexed;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.actions.NumIncrByAction;

public class NonIndexedNumericField<E, T> extends MetamodelField<E, T> {

  public NonIndexedNumericField(Field field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> incrBy(Long value) {
    return new NumIncrByAction<E>(field, value);
  }
  
  public Consumer<? super E> decrBy(Long value) {
    return new NumIncrByAction<E>(field, -value);
  }

}
