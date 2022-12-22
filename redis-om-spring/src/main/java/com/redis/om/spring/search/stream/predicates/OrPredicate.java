package com.redis.om.spring.search.stream.predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

public class OrPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<Predicate<T>> predicates = new ArrayList<>();

  public OrPredicate(SearchFieldPredicate<E, T> root) {
    predicates.add(root);
  }

  public void addPredicate(Predicate<T> predicate) {
    this.predicates.add(predicate);
  }

  public Stream<Predicate<T>> stream() {
    return predicates.stream();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Node apply(Node root) {
    Node[] nodes = stream().map(p -> ((SearchFieldPredicate) p).apply(root)).toArray(Node[]::new);
    return QueryBuilders.union(nodes);
  }

}
