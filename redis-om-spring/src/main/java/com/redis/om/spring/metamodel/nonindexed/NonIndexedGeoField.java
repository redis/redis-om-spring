package com.redis.om.spring.metamodel.nonindexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.ToggleAction;

import java.util.function.Consumer;

public class NonIndexedGeoField<E, T> extends MetamodelField<E, T> {
  public NonIndexedGeoField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }
}
