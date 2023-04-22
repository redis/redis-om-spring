package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

public class CollectionField<E, T> extends TagField<E, T> {
  public CollectionField(SearchFieldAccessor searchFieldAccessor, boolean indexed) {
    super(searchFieldAccessor, indexed);
  }
}
