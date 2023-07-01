package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.reference.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.reference.NotEqualPredicate;

public class ReferenceField<E, T> extends MetamodelField<E, T> {
  public ReferenceField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public EqualPredicate<E,T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor,value);
  }

  public NotEqualPredicate<E,T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor,value);
  }
}
