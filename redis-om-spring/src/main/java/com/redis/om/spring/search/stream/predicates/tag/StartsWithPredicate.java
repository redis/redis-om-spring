package com.redis.om.spring.search.stream.predicates.tag;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;

/**
 * A tag search predicate that filters entities where a tag field value
 * starts with a specified prefix.
 * 
 * <p>This predicate performs a prefix search on tag fields using Redis tag syntax.
 * It generates Redis search queries in the format: {@code @field:{prefix*}} which
 * matches any document where the tag field value begins with the specified text.</p>
 * 
 * <p>The predicate supports both single values and collections of values. When a
 * collection is provided, it creates an intersection query that requires all
 * prefix conditions to be met.</p>
 * 
 * <p>Special characters in the prefix are properly escaped to ensure correct
 * query behavior and prevent Redis search syntax errors.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with category tags starting with "ELEC"
 * entityStream.filter(Product$.CATEGORY.startsWith("ELEC"))
 * 
 * // Find items with all tags starting with the given prefixes
 * entityStream.filter(Item$.TAGS.startsWith(List.of("new", "sale")))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (typically String, CharSequence, or Iterable)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see EqualPredicate
 * @see InPredicate
 */
public class StartsWithPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  /** The prefix value(s) to search for */
  private final T value;

  /**
   * Creates a new StartsWithPredicate for the specified field and prefix.
   * 
   * @param field the field accessor for the target tag field
   * @param value the prefix text or collection of prefixes that tag values should start with
   */
  public StartsWithPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Returns the prefix value(s) to search for.
   * 
   * @return the prefix text or collection of prefixes
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this starts-with predicate to the given query node.
   * 
   * <p>This method generates a Redis tag prefix search query that matches documents
   * where the tag field value starts with the specified prefix. For tag fields,
   * the query uses curly braces with wildcard: {@code {prefix*}}.</p>
   * 
   * <p>When the value is an Iterable, this method creates an intersection query
   * that requires all prefix conditions to be met. Each prefix in the collection
   * is applied as a separate condition.</p>
   * 
   * <p>Special characters in the prefix are properly escaped to prevent
   * Redis search syntax errors.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the tag prefix search condition applied,
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
        and.add(getSearchAlias(), "{" + QueryUtils.escape(v.toString(), true) + "*}");
      }
      return QueryBuilders.intersect(root, and);
    } else {
      return QueryBuilders.intersect(root).add(getSearchAlias(), "{" + QueryUtils.escape(value.toString(),
          true) + "*}");
    }
  }

}
