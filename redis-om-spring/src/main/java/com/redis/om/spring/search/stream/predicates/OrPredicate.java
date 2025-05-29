package com.redis.om.spring.search.stream.predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * Logical OR predicate for combining multiple search predicates in Redis OM Spring queries.
 * <p>
 * This predicate implements a logical OR operation between multiple search field predicates,
 * allowing for complex query composition where any of the combined predicates can match.
 * The OR predicate is part of the Entity Streams fluent query API and translates to
 * a union operation in RediSearch query syntax.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * EntityStream.of(Person.class)
 * .filter(Person$.FIRST_NAME.eq("John").or(Person$.FIRST_NAME.eq("Jane")))
 * .collect(Collectors.toList());
 * </pre>
 *
 * @param <E> the entity type being queried
 * @param <T> the field type being compared
 * @see BaseAbstractPredicate
 * @see redis.clients.jedis.search.querybuilder.QueryBuilders#union(Node...)
 * @since 0.1.0
 */
public class OrPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<Predicate<T>> predicates = new ArrayList<>();

  /**
   * Constructs a new OrPredicate with an initial root predicate.
   * <p>
   * The root predicate serves as the first condition in the OR operation.
   * Additional predicates can be added using the addPredicate method.
   * </p>
   *
   * @param root the initial search field predicate to include in the OR operation
   */
  public OrPredicate(SearchFieldPredicate<E, T> root) {
    predicates.add(root);
  }

  /**
   * Adds an additional predicate to this OR operation.
   * <p>
   * The predicate will be combined with existing predicates using logical OR,
   * meaning the overall condition will match if any of the predicates evaluate to true.
   * </p>
   *
   * @param predicate the predicate to add to the OR operation
   */
  public void addPredicate(Predicate<T> predicate) {
    this.predicates.add(predicate);
  }

  /**
   * Returns a stream of all predicates in this OR operation.
   * <p>
   * This method provides access to the internal predicates for processing
   * or transformation operations.
   * </p>
   *
   * @return a Stream containing all predicates in this OR operation
   */
  public Stream<Predicate<T>> stream() {
    return predicates.stream();
  }

  /**
   * Applies this OR predicate to create a RediSearch query node.
   * <p>
   * This method transforms all contained predicates into RediSearch query nodes
   * and combines them using a union operation, which represents logical OR
   * in RediSearch query syntax.
   * </p>
   *
   * @param root the root query node to build upon
   * @return a Node representing the union (OR) of all contained predicates
   */
  @SuppressWarnings(
    "rawtypes"
  )
  @Override
  public Node apply(Node root) {
    Node[] nodes = stream().map(p -> ((SearchFieldPredicate) p).apply(root)).toArray(Node[]::new);
    return QueryBuilders.union(nodes);
  }

}
