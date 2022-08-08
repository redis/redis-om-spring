package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.redislabs.modules.rejson.Path;

public class ArrayInsertAction<E> extends BaseAbstractAction implements Consumer<E> {
  
  private Object value;
  private Long index;

  public ArrayInsertAction(Field field, Object value, Long index) {
    super(field);
    this.value = value;
    this.index = index;
  }

  @Override
  public void accept(E entity) {
    json.arrInsert(getKey(entity), Path.of("." + field.getName()), index, value);
  }

}
