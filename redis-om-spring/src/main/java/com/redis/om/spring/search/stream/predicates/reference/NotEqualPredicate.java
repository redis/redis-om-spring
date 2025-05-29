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
 * Predicate for performing "not equal" operations on reference fields.
 * This predicate excludes documents where the specified reference field
 * points to the given entity. It works by comparing the Redis key of the
 * referenced entity with the stored reference field value.
 *
 * @param <E> the entity type being queried
 * @param <T> the type of the referenced entity
 */
public class NotEqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private final Object referenceKey;
  private final T value;

  /**
   * Constructs a NotEqualPredicate for the specified field and referenced entity.
   *
   * @param field the search field accessor for the reference field to be queried
   * @param value the referenced entity that should not equal the field value
   */
  public NotEqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;

    RediSearchIndexer indexer = SpringContext.getBean(RediSearchIndexer.class);
    var keyspace = indexer.getKeyspaceForEntityClass(field.getTargetClass());
    this.referenceKey = QueryUtils.escape(getKey(keyspace, getIdFieldForEntity(value)));
  }

  /**
   * Gets the referenced entity that should not equal the field value.
   *
   * @return the referenced entity to exclude from matches
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies the "not equal" predicate to the query node tree.
   * Creates a disjunct (negated) query that excludes documents where the reference field
   * points to the specified entity. The comparison is done using the Redis key of the
   * referenced entity, with type-specific handling for different key types.
   *
   * @param root the root query node to which this predicate will be applied
   * @return the modified query node with the reference "not equal" condition applied,
   *         or the original root if the reference key type is not supported
   */
  @Override
  public Node apply(Node root) {
    Class<?> cls = referenceKey.getClass();
    if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.eq(Integer.parseInt(
          referenceKey.toString()))));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.eq(Long.parseLong(
          referenceKey.toString()))));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.eq(Double.parseDouble(
          referenceKey.toString()))));
    } else if (CharSequence.class.isAssignableFrom(cls)) {
      return QueryBuilders.intersect(root).add(QueryBuilders.disjunct(getSearchAlias(), Values.value(
          "{" + referenceKey + "}")));
    } else {
      return root;
    }
  }
}
