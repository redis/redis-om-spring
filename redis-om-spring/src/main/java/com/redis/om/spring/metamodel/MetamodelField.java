package com.redis.om.spring.metamodel;

import java.util.Comparator;
import java.util.function.Function;

public class MetamodelField<E, T> implements Comparator<E>, Function<E,T> {

  protected final SearchFieldAccessor searchFieldAccessor;
  protected final boolean indexed;
  
  public MetamodelField(SearchFieldAccessor searchFieldAccessor, boolean indexed) {
    this.searchFieldAccessor = searchFieldAccessor;
    this.indexed = indexed;
  }
  
  public SearchFieldAccessor getSearchFieldAccessor() {
    return searchFieldAccessor;
  }

  @Override
  public int compare(E o1, E o2) {
    return 0;
  }

  @Override
  public T apply(E t) {
    return null;
  }
  
  public boolean isIndexed() {
    return indexed;
  }

  public String getSearchAlias() {
    return searchFieldAccessor.getSearchAlias();
  }
}
