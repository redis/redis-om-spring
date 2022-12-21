package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

public class ArrayPopAction<E, R> extends BaseAbstractAction implements Function<E, R> {

  private Long index;

  public ArrayPopAction(SearchFieldAccessor field, Long index) {
    super(field);
    this.index = index;
  }

  @SuppressWarnings("unchecked")
  @Override
  public R apply(E entity) {
    Field f = field.getField();
    Optional<Class<?>> maybeClass = ObjectUtils.getCollectionElementType(f);
    if (maybeClass.isPresent()) {
      return (R) json.arrPop(getKey(entity), maybeClass.get(), Path.of("." + f.getName()), index);
    } else {
      throw new RuntimeException("Cannot determine contained element type for collection " + f.getName());
    }
  }
}
