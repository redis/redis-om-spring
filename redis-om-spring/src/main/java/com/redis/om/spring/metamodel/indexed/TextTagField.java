package com.redis.om.spring.metamodel.indexed;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;

public class TextTagField<E, T> extends TagField<E, T> {

  public TextTagField(Field field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> append(String value) {
    return new StringAppendAction<>(field, value);
  }

  @Override
  public ToLongFunction<? super E> length() {
    return new StrLengthAction<>(field);
  }
}
