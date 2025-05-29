package com.redis.om.spring.search.stream.predicates.fulltext;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.ObjectUtils;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Represents an "in" predicate for full-text field searches.
 * This predicate generates RediSearch queries that match documents where the specified
 * field contains any of the provided values using full-text search semantics.
 *
 * @param <E> the entity type
 * @param <T> the field value type
 */
public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<T> values;

  /**
   * Constructs a new full-text "in" predicate.
   *
   * @param field  the search field accessor
   * @param values the list of values to search for
   */
  public InPredicate(SearchFieldAccessor field, List<T> values) {
    super(field);
    this.values = values;
  }

  /**
   * Gets the list of values to search for in this predicate.
   *
   * @return the list of values used in the "in" comparison
   */
  public List<T> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    if (ObjectUtils.isNotEmpty(getValues())) {
      StringJoiner sj = new StringJoiner(" | ");
      for (Object value : getValues()) {
        sj.add(QueryUtils.escape(value.toString(), true));
      }

      return QueryBuilders.intersect(root).add(getSearchAlias(), sj.toString());
    } else
      return root;
  }

}
