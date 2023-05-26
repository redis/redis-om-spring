package com.redis.om.spring.search.stream.predicates.fulltext;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import org.apache.commons.lang3.ObjectUtils;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

import java.util.List;
import java.util.StringJoiner;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<T> values;

  public InPredicate(SearchFieldAccessor field, List<T> values) {
    super(field);
    this.values = values;
  }

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
    } else return root;
  }

}
