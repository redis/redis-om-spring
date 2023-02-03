package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;

public class VectorField <E, T> extends MetamodelField<E, T> {
  public VectorField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }
}
