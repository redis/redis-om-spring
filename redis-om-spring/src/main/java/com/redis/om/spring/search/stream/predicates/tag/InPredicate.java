package com.redis.om.spring.search.stream.predicates.tag;

import java.lang.reflect.Field;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

public class InPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<String> values;

  public InPredicate(SearchFieldAccessor field, List<String> list) {
    super(field);
    this.values = list.stream().map(QueryUtils::escape).collect(Collectors.toList());
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

    return QueryBuilder.intersect(root).add(getSearchAlias(), "{" + sj.toString() + "}");
  }
}
