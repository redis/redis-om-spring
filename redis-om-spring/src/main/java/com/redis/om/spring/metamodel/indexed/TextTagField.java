package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;
import com.redis.om.spring.search.stream.predicates.tag.EndsWithPredicate;
import com.redis.om.spring.search.stream.predicates.tag.StartsWithPredicate;

import java.util.function.Consumer;
import java.util.function.ToLongFunction;

public class TextTagField<E, T> extends TagField<E, T> {

  public TextTagField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public StartsWithPredicate<? super E,T> startsWith(T value) {
    return new StartsWithPredicate<>(searchFieldAccessor,value);
  }
  public EndsWithPredicate<? super E,T> endsWith(T value) {
    return new EndsWithPredicate<>(searchFieldAccessor,value);
  }

  public Consumer<? super E> append(String value) {
    return new StringAppendAction<>(searchFieldAccessor, value);
  }

  @Override
  public ToLongFunction<? super E> length() {
    return new StrLengthAction<>(searchFieldAccessor);
  }
}
