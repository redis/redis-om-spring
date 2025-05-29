package com.redis.om.spring.search.stream.predicates.fulltext;

import org.apache.commons.lang3.ObjectUtils;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * A full-text search predicate that filters entities where a text field value
 * starts with a specified prefix.
 * 
 * <p>This predicate performs a prefix search using wildcard matching. It generates
 * Redis search queries in the format: {@code @field:prefix*} which matches any
 * document where the field value begins with the specified text.</p>
 * 
 * <p>The predicate properly escapes special characters in the prefix to ensure
 * correct query behavior and prevent Redis search syntax errors.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with names starting with "Smart"
 * entityStream.filter(Product$.NAME.startsWith("Smart"))
 * 
 * // Find users with email addresses starting with "admin"
 * entityStream.filter(User$.EMAIL.startsWith("admin"))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (typically String or CharSequence)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see ContainingPredicate
 * @see EndsWithPredicate
 * @see LikePredicate
 */
public class StartsWithPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  /** The prefix text to search for */
  private final T value;

  /**
   * Creates a new StartsWithPredicate for the specified field and prefix.
   * 
   * @param field the field accessor for the target text field
   * @param value the prefix text that field values should start with
   */
  public StartsWithPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;
  }

  /**
   * Returns the prefix text to search for.
   * 
   * @return the prefix text
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this starts-with predicate to the given query node.
   * 
   * <p>This method generates a Redis prefix search query that matches documents
   * where the field value starts with the specified prefix. The query appends
   * a wildcard to the escaped prefix: {@code prefix*}.</p>
   * 
   * <p>Special characters in the prefix are properly escaped to prevent
   * Redis search syntax errors.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the prefix search condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    return ObjectUtils.isNotEmpty(getValue()) ?
        QueryBuilders.intersect(root).add(getSearchAlias(), QueryUtils.escape(getValue().toString(), true) + "*") :
        root;
  }

}
