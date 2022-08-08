package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.redislabs.modules.rejson.Path;

public class NumIncrByAction<E> extends BaseAbstractAction implements Consumer<E> {
  private Long value;

  public NumIncrByAction(Field field, Long value) {
    super(field);
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    json.numIncrBy(getKey(entity), Path.of("." + field.getName()), value);
  }

}
