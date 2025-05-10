package com.redis.om.spring.metamodel;

import com.redis.om.spring.search.stream.aggregations.filters.AggregationFilter;
import com.redis.om.spring.search.stream.aggregations.filters.ExistsFilter;
import com.redis.om.spring.search.stream.aggregations.filters.NotExistsFilter;
import com.redis.om.spring.search.stream.predicates.fulltext.IsMissingPredicate;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.data.domain.Sort.Order;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.function.Function;

public class MetamodelField<E, T> implements Comparator<E>, Function<E, T> {

  protected SearchFieldAccessor searchFieldAccessor;
  protected boolean indexed;
  protected String alias;
  protected Class<?> targetClass;

  public MetamodelField(SearchFieldAccessor searchFieldAccessor, boolean indexed) {
    this.searchFieldAccessor = searchFieldAccessor;
    this.indexed = indexed;
    this.alias = null;
  }

  public MetamodelField(String alias, Class<?> targetClass, boolean indexed) {
    this.searchFieldAccessor = null;
    this.indexed = indexed;
    this.alias = alias;
    this.targetClass = targetClass;
  }

  public MetamodelField(String alias, Class<?> targetClass) {
    this.searchFieldAccessor = null;
    this.indexed = false;
    this.alias = alias;
    this.targetClass = targetClass;
  }

  public MetamodelField(Class<E> targetClass, String fieldName) {
    Field field;
    try {
      field = ObjectUtils.getDeclaredFieldTransitively(targetClass, fieldName);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    SearchFieldAccessor sfa = new SearchFieldAccessor(fieldName, "$."+fieldName, field);
    this.searchFieldAccessor = sfa;
    this.indexed = true;
    this.alias = fieldName;
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
    return searchFieldAccessor != null ? searchFieldAccessor.getTargetClass() : targetClass;
  }

  public String getJSONPath() {
    return searchFieldAccessor != null ? searchFieldAccessor.getJsonPath() : "";
  }

  public Order asc() {
    return Order.asc("@" + getSearchAlias());
  }

  public Order desc() {
    return Order.desc("@" + getSearchAlias());
  }

  public AggregationFilter exists() {
    return new ExistsFilter(this.getSearchAlias());
  }

  public AggregationFilter notExists() {
    return new NotExistsFilter(this.getSearchAlias());
  }

  public IsMissingPredicate<E, T> isMissing() {
    return new IsMissingPredicate<>(searchFieldAccessor);
  }
}
