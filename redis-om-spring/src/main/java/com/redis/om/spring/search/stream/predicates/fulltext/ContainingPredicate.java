package com.redis.om.spring.search.stream.predicates.fulltext;

import org.apache.commons.lang3.ObjectUtils;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * A full-text search predicate that filters entities where a text field contains
 * a specified substring anywhere within the field value.
 * 
 * <p>This predicate performs a case-sensitive substring search using wildcard
 * matching. It generates Redis search queries in the format: {@code @field:*substring*}
 * which matches any document where the field contains the specified text.</p>
 * 
 * <p>This predicate is designed for use with fields annotated with {@code @TextIndexed}
 * or {@code @Searchable}, which enable full-text search capabilities in Redis.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with "wireless" in the description
 * entityStream.filter(Product$.DESCRIPTION.containing("wireless"))
 * 
 * // Find users with "smith" in their name
 * entityStream.filter(User$.NAME.containing("smith"))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (typically String or CharSequence)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see StartsWithPredicate
 * @see EndsWithPredicate
 * @see LikePredicate
 */
public class ContainingPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  /** The substring to search for within the field value */
  private final T value;

  /**
   * Creates a new ContainingPredicate for the specified field and substring.
   * 
   * @param field the field accessor for the target text field
   * @param value the substring to search for within the field value
   */
  public ContainingPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Returns the substring to search for.
   * 
   * @return the search substring
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this containing predicate to the given query node.
   * 
   * <p>This method generates a Redis wildcard search query that matches documents
   * where the field value contains the specified substring. The query wraps the
   * search term with wildcards: {@code *substring*}.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the substring search condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    return ObjectUtils.isNotEmpty(getValue()) ?
        QueryBuilders.intersect(root).add(getSearchAlias(), "*" + getValue().toString() + "*") :
        root;
  }

}
