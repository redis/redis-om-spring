package com.redis.om.spring.metamodel.indexed;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.redis.om.spring.search.stream.actions.StringAppendAction;

public class TextTagField<E, T> extends TagField<E, T> {

  public TextTagField(Field field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> append(String value) {
    return new StringAppendAction<E>(field, value);
  }
}
