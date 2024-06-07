package com.redis.om.spring.search.stream.predicates.reference;

import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.util.SpringContext;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.Values;

import static com.redis.om.spring.util.ObjectUtils.getIdFieldForEntity;
import static com.redis.om.spring.util.ObjectUtils.getKey;

public class EqualPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  private final Object referenceKey;
  private final T value;

  public EqualPredicate(SearchFieldAccessor field, T value) {
    super(field);
    this.value = value;

    RediSearchIndexer indexer = SpringContext.getBean(RediSearchIndexer.class);
    var keyspace = indexer.getKeyspaceForEntityClass(field.getTargetClass());
    this.referenceKey = QueryUtils.escape(getKey(keyspace, getIdFieldForEntity(value)));
  }

  public T getValue() {
    return value;
  }

  @Override
  public Node apply(Node root) {
    Class<?> cls = referenceKey.getClass();
    if (cls == Integer.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Integer.parseInt(referenceKey.toString())));
    } else if (cls == Long.class) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), Values.eq(Long.parseLong(referenceKey.toString())));
    } else if (cls == Double.class) {
      return QueryBuilders.intersect(root)
          .add(getSearchAlias(), Values.eq(Double.parseDouble(referenceKey.toString())));
    } else if (CharSequence.class.isAssignableFrom(cls)) {
      return QueryBuilders.intersect(root).add(getSearchAlias(), "{" + referenceKey + "}");
    } else {
      return root;
    }
  }
}
