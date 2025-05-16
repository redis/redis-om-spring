package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.json.Path2;

public class ArrayPopAction<E, R> extends BaseAbstractAction implements Function<E, R> {

  private final Integer index;

  public ArrayPopAction(SearchFieldAccessor field, Integer index) {
    super(field);
    this.index = index;
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public R apply(E entity) {
    Field f = field.getField();
    Optional<Class<?>> maybeClass = ObjectUtils.getCollectionElementClass(f);
    if (maybeClass.isPresent()) {
      var popResult = json.arrPop(getKey(entity), maybeClass.get(), Path2.of("." + f.getName()), index);
      return popResult != null && !popResult.isEmpty() ? (R) popResult.get(0) : null;
    } else {
      throw new RuntimeException("Cannot determine contained element type for collection " + f.getName());
    }
  }
}
