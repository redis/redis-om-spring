package com.redis.om.spring.metamodel.indexed;

import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;

public class TextTagField<E, T> extends TagField<E, T> {

  public TextTagField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> append(String value) {
    return new StringAppendAction<>(searchFieldAccessor, value);
  }

  @Override
  public ToLongFunction<? super E> length() {
    return new StrLengthAction<>(searchFieldAccessor);
  }
}
