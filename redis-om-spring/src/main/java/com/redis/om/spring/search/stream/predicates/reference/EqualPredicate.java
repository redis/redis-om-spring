package com.redis.om.spring.search.stream.predicates.reference;

import static com.redis.om.spring.util.ObjectUtils.getIdFieldForEntity;
import static com.redis.om.spring.util.ObjectUtils.getKey;

import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.util.SpringContext;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

/**
 * A reference equality predicate that filters entities based on entity references
 * (foreign key relationships) by matching against the referenced entity's ID.
 * 
 * <p>This predicate is designed for use with reference fields that store relationships
 * to other Redis OM entities. It automatically extracts the ID from the referenced
 * entity and searches for that ID value in the reference field.</p>
 * 
 * <p>The predicate resolves the entity keyspace and properly formats the entity key
 * for searching, handling both numeric and string-based entity IDs.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find orders for a specific customer entity
 * Customer customer = customerRepository.findById(customerId);
 * entityStream.filter(Order$.CUSTOMER.eq(customer))
 * 
 * // Find products in a specific category entity
 * Category electronics = categoryRepository.findByName("Electronics");
 * entityStream.filter(Product$.CATEGORY.eq(electronics))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the referenced entity type
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see NotEqualPredicate
 * @see com.redis.om.spring.indexing.RediSearchIndexer
 */
public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The resolved Redis key for the referenced entity */
  private final Object referenceKey;

  /** The referenced entity object */
  private final T value;

  /**
   * Creates a new EqualPredicate for entity reference matching.
   * 
   * <p>This constructor extracts the ID from the referenced entity and resolves
   * the appropriate Redis key format for searching.</p>
   * 
   * @param field the field accessor for the target reference field
   * @param value the referenced entity to match against
   */
  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;

    RediSearchIndexer indexer = SpringContext.getBean(RediSearchIndexer.class);
    var keyspace = indexer.getKeyspaceForEntityClass(field.getTargetClass());
    this.referenceKey = QueryUtils.escape(getKey(keyspace, getIdFieldForEntity(value)));
  }

  /**
   * Returns the referenced entity object.
   * 
   * @return the referenced entity
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this reference equality predicate to the given query node.
   * 
   * <p>This method generates a Redis search query that matches the resolved
   * entity key against the reference field. The query format depends on the
   * key type (numeric or string) and uses appropriate escaping for string keys.</p>
   * 
   * <p>For string-based keys, the query wraps the key in curly braces to ensure
   * proper tag field matching.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the reference equality condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    Class<?> cls = referenceKey.getClass();
    if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Integer.parseInt(referenceKey.toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Long.parseLong(referenceKey.toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Double.parseDouble(referenceKey
          .toString())));
    } else if (CharSequence.class.isAssignableFrom(cls)) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), "{" + referenceKey + "}");
    } else {
      return root;
    }
  }
}
