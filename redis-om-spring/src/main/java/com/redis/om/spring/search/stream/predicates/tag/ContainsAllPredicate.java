package com.redis.om.spring.search.stream.predicates.tag;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.List;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;

/**
 * Predicate for tag fields that must contain all specified values.
 * <p>
 * This predicate creates a query that matches documents where the target tag field
 * contains ALL of the specified values. It's useful for intersection-based queries
 * on tag fields where you need documents that have multiple specific tags.
 * </p>
 * <p>
 * The predicate builds an AND (intersection) query where each value must be present
 * in the tag field. For a document to match, its tag field must contain every
 * value in the provided list.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Find products that have ALL these tags: "electronics", "mobile", "smartphone"
 * entityStream.filter(Product$.tags.containsAll(List.of("electronics", "mobile", "smartphone")))
 * 
 * // This would match a product with tags: ["electronics", "mobile", "smartphone", "android"]
 * // But NOT match a product with tags: ["electronics", "mobile"] (missing "smartphone")
 * }</pre>
 *
 * @param <E> the entity type
 * @param <T> the field type
 * @see BaseAbstractPredicate
 * @see SearchFieldAccessor
 * @since 0.1.0
 */
public class ContainsAllPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<String> values;

  /**
   * Creates a new ContainsAllPredicate for the specified field and values.
   * <p>
   * The provided values are automatically escaped to handle special characters
   * in Redis Search queries.
   * </p>
   *
   * @param field the search field accessor for the target tag field
   * @param list  the list of values that must all be present in the field
   */
  public ContainsAllPredicate(SearchFieldAccessor field, List<String> list) {
    super(field);
    this.values = list.stream().map(QueryUtils::escape).toList();
  }

  /**
   * Gets the list of values that must all be contained in the tag field.
   *
   * @return the escaped list of values
   */
  public List<String> getValues() {
    return values;
  }

  /**
   * Applies this predicate to the query builder node structure.
   * <p>
   * Creates an intersection (AND) query that requires all specified values
   * to be present in the tag field. If no values are provided, returns
   * the root node unchanged.
   * </p>
   *
   * @param root the root query node to extend
   * @return a new query node representing the intersection of the root and this predicate
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValues()))
      return root;
    QueryNode and = QueryBuilders.intersect();
    for (String value : getValues()) {
      and.add(getSearchAlias(), "{" + value + "}");
    }

    return QueryBuilders.intersect(root, and);
  }
}
