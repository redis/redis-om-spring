package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayPopAction<E, R> extends BaseAbstractAction implements Function<E, R> {

  private Long index;

  public ArrayPopAction(Field field, Long index) {
    super(field);
    this.index = index;
  }

  @SuppressWarnings("unchecked")
  @Override
  public R apply(E entity) {
    Optional<Class<?>> maybeClass = ObjectUtils.getCollectionElementType(field);
    if (maybeClass.isPresent()) {
      return (R) json.arrPop(getKey(entity), maybeClass.get(), Path.of("." + field.getName()), index);
    } else {
      throw new RuntimeException("Cannot determine contained element type for collection " + field.getName());
    }
  }
}
