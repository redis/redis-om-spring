package com.redis.om.spring.search.stream.predicates.tag;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.List;
import java.util.StringJoiner;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Represents an "in" predicate for tag field searches.
 * This predicate generates RediSearch queries that match documents where the specified
 * tag field contains any of the provided tag values. Tag fields support exact matching
 * and are optimized for categorical data.
 *
 * @param <E> the entity type
 * @param <T> the field value type
 */
public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<String> values;

  /**
   * Constructs a new tag "in" predicate.
   *
   * @param field the search field accessor
   * @param list  the list of tag values to search for
   */
  public InPredicate(SearchFieldAccessor field, List<String> list) {
    super(field);
    this.values = list.stream().map(QueryUtils::escape).toList();
  }

  /**
   * Gets the list of escaped tag values to search for in this predicate.
   *
   * @return the list of escaped tag values used in the "in" comparison
   */
  public List<String> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    if (isEmpty(getValues()))
      return root;
    StringJoiner sj = new StringJoiner(" | ");
    for (Object value : getValues()) {
      sj.add(value.toString());
    }

    return QueryBuilders.intersect(root).add(getSearchAlias(), "{" + sj + "}");
  }
}
