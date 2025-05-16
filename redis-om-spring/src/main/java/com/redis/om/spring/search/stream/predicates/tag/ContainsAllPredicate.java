package com.redis.om.spring.search.stream.predicates.tag;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.List;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;

public class ContainsAllPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<String> values;

  public ContainsAllPredicate(SearchFieldAccessor field, List<String> list) {
    super(field);
    this.values = list.stream().map(QueryUtils::escape).toList();
  }

  public List<String> getValues() {
    return values;
  }

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
