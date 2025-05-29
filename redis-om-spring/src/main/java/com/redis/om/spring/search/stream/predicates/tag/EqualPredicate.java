package com.redis.om.spring.search.stream.predicates.tag;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;

/**
 * A tag equality predicate that filters entities where a tag field exactly
 * matches one or more specified values.
 * 
 * <p>This predicate is designed for use with fields annotated with {@code @TagIndexed}
 * or fields that are automatically indexed as tags (such as enum fields, strings, and
 * collections). Tag searches are typically faster than full-text searches and support
 * exact matching semantics.</p>
 * 
 * <p>This predicate supports both single values and collections. When a collection
 * is provided, it matches documents where the tag field contains all the specified
 * values (AND logic).</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with specific category
 * entityStream.filter(Product$.CATEGORY.eq("electronics"))
 * 
 * // Find users with specific tags
 * entityStream.filter(User$.TAGS.eq(Arrays.asList("premium", "active")))
 * 
 * // Find products with enum status
 * entityStream.filter(Product$.STATUS.eq(Status.AVAILABLE))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (typically String, Enum, or Collection)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see InPredicate
 * @see ContainsAllPredicate
 */
public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The value or collection of values to match against */
  private final T value;

  /**
   * Creates a new EqualPredicate for the specified field and value(s).
   * 
   * @param field the field accessor for the target tag field
   * @param value the value to match, or collection of values for AND matching
   */
  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Returns the value or collection of values to match against.
   * 
   * @return the target value(s) for equality comparison
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this tag equality predicate to the given query node.
   * 
   * <p>This method generates Redis tag search queries that perform exact matching.
   * The behavior depends on the value type:</p>
   * 
   * <ul>
   * <li>For single values: {@code @field:{"value"}}</li>
   * <li>For collections: intersects multiple tag conditions with AND logic</li>
   * </ul>
   * 
   * <p>Tag values are wrapped in curly braces and quotes to ensure proper
   * Redis tag search syntax.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the tag equality condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue()))
      return root;
    if (Iterable.class.isAssignableFrom(getValue().getClass())) {
      Iterable<?> values = (Iterable<?>) getValue();
      QueryNode and = QueryBuilders.intersect();
      for (Object v : values) {
        and.add(getSearchAlias(), "{\"" + v.toString() + "\"}");
      }
      return QueryBuilders.intersect(root, and);
    } else {
      return QueryBuilders.intersect(root).add(getSearchAlias(), "{\"" + value.toString() + "\"}");
    }
  }

}