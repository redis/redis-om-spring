package com.redis.om.spring.metamodel.nonindexed;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;

public class NonIndexedTextField<E, T> extends MetamodelField<E, T> {


  public NonIndexedTextField(Field field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> append(String value) {
    return new StringAppendAction<>(field, value);
  }

  public ToLongFunction<? super E> length() {
    return new StrLengthAction<>(field);
  }

}
