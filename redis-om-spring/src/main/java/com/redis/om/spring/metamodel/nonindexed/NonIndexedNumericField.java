package com.redis.om.spring.metamodel.nonindexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.NumIncrByAction;

import java.util.function.Consumer;

public class NonIndexedNumericField<E, T> extends MetamodelField<E, T> {

  public NonIndexedNumericField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<E> incrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, value);
  }
  
  public Consumer<E> decrBy(Long value) {
    return new NumIncrByAction<>(searchFieldAccessor, -value);
  }

}
