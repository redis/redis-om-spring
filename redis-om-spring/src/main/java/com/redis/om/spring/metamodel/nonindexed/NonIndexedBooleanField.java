package com.redis.om.spring.metamodel.nonindexed;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.actions.ToggleAction;

public class NonIndexedBooleanField<E, T> extends MetamodelField<E, T> {
  public NonIndexedBooleanField(Field field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> toggle() {
    return new ToggleAction<>(field);
  }
}
