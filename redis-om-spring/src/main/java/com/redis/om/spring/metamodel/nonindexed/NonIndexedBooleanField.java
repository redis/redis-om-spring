package com.redis.om.spring.metamodel.nonindexed;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.ToggleAction;

public class NonIndexedBooleanField<E, T> extends MetamodelField<E, T> {
  public NonIndexedBooleanField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<E> toggle() {
    return new ToggleAction<>(searchFieldAccessor);
  }
}
