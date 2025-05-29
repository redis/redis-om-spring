package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.function.Function;

import org.springframework.data.domain.Sort.Order;

import com.redis.om.spring.search.stream.aggregations.filters.AggregationFilter;
import com.redis.om.spring.search.stream.aggregations.filters.ExistsFilter;
import com.redis.om.spring.search.stream.aggregations.filters.NotExistsFilter;
import com.redis.om.spring.search.stream.predicates.fulltext.IsMissingPredicate;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Base class for metamodel fields that provide search and query capabilities.
 *
 * @param <E> the entity type
 * @param <T> the field type
 */
public class MetamodelField<E, T> implements Comparator<E>, Function<E, T> {

  /** The search field accessor for this metamodel field */
  protected SearchFieldAccessor searchFieldAccessor;

  /** Whether this field is indexed for search operations */
  protected boolean indexed;

  /** The alias name for this field */
  protected String alias;

  /** The target class that contains this field */
  protected Class<?> targetClass;

  /**
   * Creates a metamodel field with a search field accessor.
   *
   * @param searchFieldAccessor the search field accessor
   * @param indexed             whether the field is indexed
   */
  public MetamodelField(SearchFieldAccessor searchFieldAccessor, boolean indexed) {
    this.searchFieldAccessor = searchFieldAccessor;
    this.indexed = indexed;
    this.alias = null;
  }

  /**
   * Creates a metamodel field with an alias, target class, and indexing flag.
   *
   * @param alias       the field alias
   * @param targetClass the target class containing the field
   * @param indexed     whether the field is indexed
   */
  public MetamodelField(String alias, Class<?> targetClass, boolean indexed) {
    this.searchFieldAccessor = null;
    this.indexed = indexed;
    this.alias = alias;
    this.targetClass = targetClass;
  }

  /**
   * Creates a non-indexed metamodel field with an alias and target class.
   *
   * @param alias       the field alias
   * @param targetClass the target class containing the field
   */
  public MetamodelField(String alias, Class<?> targetClass) {
    this.searchFieldAccessor = null;
    this.indexed = false;
    this.alias = alias;
    this.targetClass = targetClass;
  }

  /**
   * Creates an indexed metamodel field by reflecting on the target class to find the field.
   *
   * @param targetClass the target class containing the field
   * @param fieldName   the name of the field
   * @throws RuntimeException if the field cannot be found
   */
  public MetamodelField(Class<E> targetClass, String fieldName) {
    Field field;
    try {
      field = ObjectUtils.getDeclaredFieldTransitively(targetClass, fieldName);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    SearchFieldAccessor sfa = new SearchFieldAccessor(fieldName, "$." + fieldName, field);
    this.searchFieldAccessor = sfa;
    this.indexed = true;
    this.alias = fieldName;
  }

  /**
   * Gets the search field accessor for this metamodel field.
   *
   * @return the search field accessor
   */
  public SearchFieldAccessor getSearchFieldAccessor() {
    return searchFieldAccessor;
  }

  /**
   * Compares two entities. Default implementation returns 0 (equal).
   *
   * @param o1 the first entity to compare
   * @param o2 the second entity to compare
   * @return 0 indicating equal comparison
   */
  @Override
  public int compare(E o1, E o2) {
    return 0;
  }

  /**
   * Applies this function to extract the field value from an entity.
   * Default implementation returns null.
   *
   * @param t the entity to extract the field value from
   * @return null in the default implementation
   */
  @Override
  public T apply(E t) {
    return null;
  }

  /**
   * Checks whether this field is indexed for search operations.
   *
   * @return true if the field is indexed, false otherwise
   */
  public boolean isIndexed() {
    return indexed;
  }

  /**
   * Gets the search alias for this field.
   *
   * @return the search alias from the accessor if available, otherwise the alias
   */
  public String getSearchAlias() {
    return searchFieldAccessor != null ? searchFieldAccessor.getSearchAlias() : alias;
  }

  /**
   * Gets the target class that contains this field.
   *
   * @return the target class from the accessor if available, otherwise the targetClass
   */
  public Class<?> getTargetClass() {
    return searchFieldAccessor != null ? searchFieldAccessor.getTargetClass() : targetClass;
  }

  /**
   * Gets the JSON path for this field.
   *
   * @return the JSON path from the accessor if available, otherwise an empty string
   */
  public String getJSONPath() {
    return searchFieldAccessor != null ? searchFieldAccessor.getJsonPath() : "";
  }

  /**
   * Creates an ascending sort order for this field.
   *
   * @return an ascending sort order
   */
  public Order asc() {
    return Order.asc("@" + getSearchAlias());
  }

  /**
   * Creates a descending sort order for this field.
   *
   * @return a descending sort order
   */
  public Order desc() {
    return Order.desc("@" + getSearchAlias());
  }

  /**
   * Creates an aggregation filter that checks if this field exists.
   *
   * @return an exists aggregation filter
   */
  public AggregationFilter exists() {
    return new ExistsFilter(this.getSearchAlias());
  }

  /**
   * Creates an aggregation filter that checks if this field does not exist.
   *
   * @return a not exists aggregation filter
   */
  public AggregationFilter notExists() {
    return new NotExistsFilter(this.getSearchAlias());
  }

  /**
   * Creates a predicate that checks if this field is missing from an entity.
   *
   * @return an is missing predicate
   */
  public IsMissingPredicate<E, T> isMissing() {
    return new IsMissingPredicate<>(searchFieldAccessor);
  }
}
