package com.redis.om.spring.search.stream.predicates.fulltext;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;

public class IsMissingPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  public IsMissingPredicate(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public Node apply(Node root) {
    String query = String.format("ismissing(@%s)", getSearchAlias());

    return new Node() {
      @Override
      public String toString() {
        return query;
      }

      @Override
      public String toString(Parenthesize mode) {
        return query;
      }
    };
  }
}