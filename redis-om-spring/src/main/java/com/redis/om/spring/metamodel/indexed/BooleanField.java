package com.redis.om.spring.metamodel.indexed;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.redis.om.spring.search.stream.actions.ToggleAction;

public class BooleanField<E, T> extends TagField<E, T> {
  public BooleanField(Field field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> toggle() {
    return new ToggleAction<E>(field);
  }
}
