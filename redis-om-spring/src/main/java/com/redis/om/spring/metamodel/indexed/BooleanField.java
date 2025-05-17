package com.redis.om.spring.metamodel.indexed;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.ToggleAction;

public class BooleanField<E, T> extends TagField<E, T> {
  public BooleanField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public BooleanField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  public Consumer<E> toggle() {
    return new ToggleAction<>(searchFieldAccessor);
  }
}
