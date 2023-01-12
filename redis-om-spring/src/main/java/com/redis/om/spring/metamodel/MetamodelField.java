package com.redis.om.spring.metamodel;

import org.springframework.data.domain.Sort.Order;

import java.util.Comparator;
import java.util.function.Function;

public class MetamodelField<E, T> implements Comparator<E>, Function<E,T> {

  protected final SearchFieldAccessor searchFieldAccessor;
  protected final boolean indexed;
  protected final String alias;
  
  public MetamodelField(SearchFieldAccessor searchFieldAccessor, boolean indexed) {
    this.searchFieldAccessor = searchFieldAccessor;
    this.indexed = indexed;
    this.alias = null;
  }

  public MetamodelField(String alias) {
    this.searchFieldAccessor = null;
    this.indexed = false;
    this.alias = alias;
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
    return searchFieldAccessor != null ? searchFieldAccessor.getSearchAlias() : alias;
  }

  public Class<?> getTargetClass() {
    return searchFieldAccessor != null ? searchFieldAccessor.getTargetClass() : String.class;
  }

  public Order asc() {
    return Order.asc("@" + getSearchAlias());
  }

  public Order desc() {
    return Order.desc("@" + getSearchAlias());
  }
}
