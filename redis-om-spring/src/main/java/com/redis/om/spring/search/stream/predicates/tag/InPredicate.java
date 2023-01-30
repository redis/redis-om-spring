package com.redis.om.spring.search.stream.predicates.tag;

import java.util.List;
import java.util.StringJoiner;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<String> values;

  public InPredicate(SearchFieldAccessor field, List<String> list) {
    super(field);
    this.values = list.stream().map(QueryUtils::escape).toList();
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public Node apply(Node root) {
    StringJoiner sj = new StringJoiner(" | ");
    for (Object value : getValues()) {
      sj.add(value.toString());
    }

    return QueryBuilders.intersect(root).add(getSearchAlias(), "{" + sj + "}");
  }
}
